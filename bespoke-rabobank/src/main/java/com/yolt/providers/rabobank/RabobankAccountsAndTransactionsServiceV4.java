package com.yolt.providers.rabobank;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.rabobank.dto.AccessTokenResponseDTO;
import com.yolt.providers.rabobank.dto.external.*;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RabobankAccountsAndTransactionsServiceV4 implements RabobankAccountsAndTransactionsService {

    private static final List<String> AIS_HEADERS_TO_SIGN = Arrays.asList("digest", "date", "x-request-id");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter RABOBANK_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O", Locale.ENGLISH);
    // Agreed with Leon to use static (dutch) naming for account name
    private static final String ACCOUNT_NAME = "Rabobank rekening";

    private final Clock clock;

    @Override
    public List<ProviderAccountDTO> getAccountsAndTransactions(final RestTemplate restTemplate,
                                                               final Instant from,
                                                               final String psuIpAddress,
                                                               final AccessTokenResponseDTO accessToken,
                                                               final RabobankAuthenticationMeans authenticationMeans, Signer signer) throws CertificateEncodingException {

        String encodedCertificate = Base64.toBase64String(authenticationMeans.getClientSigningCertificate().getEncoded());

        HttpHeaders httpHeaders = createHeaders(accessToken, psuIpAddress, authenticationMeans, new byte[]{}, signer, encodedCertificate);
        AccountList accountList = restTemplate.exchange("/payments/account-information/ais/accounts", HttpMethod.GET, new HttpEntity<>(httpHeaders), AccountList.class).getBody();
        Optional<AccountList> accounts = Optional.ofNullable(accountList);

        return accounts.map(it -> it.getAccounts()
                        .stream()
                        .map(account -> convertAccountToDTO(account, restTemplate, from, httpHeaders))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private ProviderAccountDTO convertAccountToDTO(final AccountDetails account, final RestTemplate restTemplate, final Instant fromDate, final HttpHeaders httpHeaders) {
        String accountId = account.getResourceId();
        Optional<BalanceList> balances = Optional.ofNullable(restTemplate.exchange("/payments/account-information/ais/accounts/{account-id}/balances", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                ReadBalanceResponse.class, Map.of("account-id", accountId)).getBody().getBalances());
        List<AccountReport> transactions = getTransactionsForAccount(restTemplate, accountId, fromDate, httpHeaders);
        List<ProviderTransactionDTO> transactionDTOS = transactions.stream()
                .map(this::convertTransactionList)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Map<BalanceType, Balance> balanceMap = balances.orElse(new BalanceList())
                .stream()
                .collect(Collectors.toMap(Balance::getBalanceType, balance -> balance));
        BigDecimal currentBalance = Optional.ofNullable(balanceMap.get(BalanceType.EXPECTED))
                .map(it -> new BigDecimal(it.getBalanceAmount().getAmount()))
                .orElse(BigDecimal.ZERO);

        return ProviderAccountDTO.builder()
                .lastRefreshed(Instant.now(clock).atZone(ZoneOffset.UTC))
                .accountNumber(convertAccountNumber(account))
                .name(ACCOUNT_NAME)
                .closed(!AccountStatus.ENABLED.equals(account.getStatus()))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .currentBalance(currentBalance)
                .currency(CurrencyCode.valueOf(account.getCurrency()))
                .accountId(accountId)
                .transactions(transactionDTOS)
                .extendedAccount(ExtendedModelMapperV3.mapToExtendedModelAccount(account, balances.orElse(new BalanceList())))
                .build();
    }

    private static ProviderAccountNumberDTO convertAccountNumber(final AccountDetails accountDetails) {
        if (StringUtils.isBlank(accountDetails.getIban())) {
            return null;
        }
        final ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, accountDetails.getIban());
        providerAccountNumberDTO.setHolderName(accountDetails.getOwnerName());
        return providerAccountNumberDTO;
    }

    private List<ProviderTransactionDTO> convertTransactionList(final AccountReport transactions) {
        List<ProviderTransactionDTO> transactionDTOS = new ArrayList<>();

        if (transactions.getBooked() != null) {
            for (TransactionDetails transaction : transactions.getBooked()) {
                transactionDTOS.add(convertTransactionToDTO(transaction, TransactionStatus.BOOKED));
            }
        }

        if (transactions.getPending() != null) {
            for (TransactionDetails transaction : transactions.getPending()) {
                transactionDTOS.add(convertTransactionToDTO(transaction, TransactionStatus.PENDING));
            }
        }

        return transactionDTOS;
    }

    private ProviderTransactionDTO convertTransactionToDTO(final TransactionDetails transaction, final TransactionStatus status) {
        // XXX Either either one or both of these fields can be null so we want to make sure we include all the information we get..
        String remittanceInformationStructured = Optional.ofNullable(transaction.getRemittanceInformationStructured())
                .map(it -> it + "\n")
                .orElse("");
        String remittanceInformationUnstructured = Optional.ofNullable(transaction.getRemittanceInformationUnstructured())
                .orElse("");
        BigDecimal amount = new BigDecimal(transaction.getTransactionAmount().getAmount());
        String description = remittanceInformationStructured + remittanceInformationUnstructured;

        var bankSpecific = Optional.ofNullable(transaction.getPaymentInformationIdentification())
                .map(id -> Map.of("paymentInformationIdentification", transaction.getPaymentInformationIdentification()))
                .orElse(null);

        return ProviderTransactionDTO.builder()
                .bankSpecific(bankSpecific)
                .externalId(transaction.getEntryReference())
                .dateTime(LocalDate.parse(transaction.getBookingDate()).atStartOfDay(ZoneOffset.UTC))
                .type(amount.compareTo(BigDecimal.ZERO) > 0
                        ? ProviderTransactionType.CREDIT
                        : ProviderTransactionType.DEBIT)
                .category(YoltCategory.GENERAL)
                .amount(amount.abs())
                .description(description)
                .status(status)
                .extendedTransaction(ExtendedModelMapperV3.mapToExtendedTransaction(transaction, status))
                .build();
    }

    /**
     * @see <a href="https://developer-sandbox.rabobank.nl/node/1539"></a>
     */
    private List<AccountReport> getTransactionsForAccount(final RestTemplate restTemplate, final String accountId, final Instant fromDate, final HttpHeaders httpHeaders) {
        String nextPage = String.format("/payments/account-information/ais/accounts/%s/transactions?bookingStatus=booked&dateFrom=%s", accountId, DATE_TIME_FORMATTER.format(fromDate));
        List<AccountReport> allTransactions = new ArrayList<>();
        do {
            TransactionsResponse200Json response = restTemplate.exchange(nextPage, HttpMethod.GET, new HttpEntity<>(httpHeaders), TransactionsResponse200Json.class).getBody();

            if (response != null) {
                Optional<AccountReport> transactions = Optional.ofNullable(response.getTransactions());
                transactions.ifPresent(allTransactions::add);
                nextPage = transactions
                        .map(AccountReport::getLinks)
                        .map(LinksAccountReport::getNext)
                        .map(HrefType::getHref)
                        .map(path -> "/payments/account-information/ais" + path)
                        .orElse("");
            } else {
                nextPage = "";
            }
        } while (!nextPage.isEmpty());
        return allTransactions;
    }

    private HttpHeaders createHeaders(final AccessTokenResponseDTO accessToken,
                                      final String psuIpAddress,
                                      final RabobankAuthenticationMeans authenticationMeans,
                                      final byte[] body, Signer signer,
                                      final String encodedSigningCertificate) {
        HttpHeaders headers = new HttpHeaders();
        X509Certificate clientSigningCertificate = authenticationMeans.getClientSigningCertificate();

        headers.add("x-request-id", ExternalTracingUtil.createLastExternalTraceId());
        headers.add("x-ibm-client-id", authenticationMeans.getClientId());
        headers.add("authorization", "Bearer " + accessToken.getAccessToken());
        headers.add("tpp-signature-certificate", encodedSigningCertificate);
        headers.add("date", RABOBANK_DATETIME_FORMATTER.format(ZonedDateTime.now(clock)));
        headers.add("digest", SigningUtil.getDigest(body));
        headers.add("signature", SigningUtil.getSigningString(signer, headers, clientSigningCertificate.getSerialNumber().toString(), authenticationMeans.getSigningKid(), AIS_HEADERS_TO_SIGN));
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.add("PSU-IP-Address", psuIpAddress);
        }
        return headers;
    }
}
