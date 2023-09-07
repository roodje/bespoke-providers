package com.yolt.providers.bunq.common.service.fetchdata;

import com.yolt.providers.bunq.common.auth.BunqApiContext;
import com.yolt.providers.bunq.common.http.BunqHttpServiceV5;
import com.yolt.providers.bunq.common.mapper.ExtendedModelMapper;
import com.yolt.providers.bunq.common.model.MonetaryAccountResponse;
import com.yolt.providers.bunq.common.model.MonetaryAccountResponse.MonetaryAccount;
import com.yolt.providers.bunq.common.model.PaginatedResponse;
import com.yolt.providers.bunq.common.model.TransactionsResponse;
import com.yolt.providers.bunq.common.model.TransactionsResponse.Transaction;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BunqAccountsAndTransactionsServiceV5 {

    private final Clock clock;

    private static final ZoneId AMSTERDAM_TIMEZONE = ZoneId.of("Europe/Amsterdam");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    public List<ProviderAccountDTO> fetchAccountsAndTransactionsForUser(final BunqApiContext context,
                                                                        final BunqHttpServiceV5 httpService,
                                                                        final Instant transactionsFetchStartTime) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> allAccounts = new ArrayList<>();

        String olderAccountsId = null;

        do {
            String externalAccountId = null;
            try {
                MonetaryAccountResponse response = httpService.getAccounts(olderAccountsId, context);
                olderAccountsId = determineFromId(response);
                for (MonetaryAccount account : response.getMonetaryAccounts()) {
                    // https://yolt.atlassian.net/browse/C4PO-10091 high priority production incident, fast fix
                    if (account != null && account.getCurrency() != null) {
                        ProviderAccountNumberDTO accountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
                        accountNumberDTO.setHolderName(account.getHolderName());

                        externalAccountId = account.getId();
                        allAccounts.add(ProviderAccountDTO.builder()
                                .accountId(account.getId())
                                .name(account.getDescription())
                                .accountNumber(accountNumberDTO)
                                .currency(CurrencyCode.valueOf(account.getCurrency()))
                                .currentBalance(account.getBalance().getValue())
                                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                                .lastRefreshed(ZonedDateTime.now(clock))
                                .transactions(fetchTransactionsForAccount(httpService, context, account.getId(), transactionsFetchStartTime))
                                .extendedAccount(ExtendedModelMapper.mapToExtendedModelAccount(account))
                                .build());
                    }
                }
            } catch (HttpStatusCodeException e) {
                if (HttpStatus.FORBIDDEN.equals(e.getStatusCode()) || HttpStatus.UNAUTHORIZED.equals(e.getStatusCode())) {
                    String message = String.format("Request to bunq failed because token was invalid, statuscode: %s", e.getStatusCode().toString());
                    throw new TokenInvalidException(message);
                }
                throw new ProviderFetchDataException(e);
            }
        } while (olderAccountsId != null);

        return allAccounts;
    }

    public List<ProviderTransactionDTO> fetchTransactionsForAccount(final BunqHttpServiceV5 httpServiceV5,
                                                                    final BunqApiContext context,
                                                                    final String accountId,
                                                                    final Instant transactionsFetchStartTime) throws TokenInvalidException {
        List<Transaction> allTransactions = new ArrayList<>();
        // XXX Bunq sorts the transactions from newest to oldest so we have to use the oldertransactionsurl to get more transactions.
        String olderTransactionsId = null;
        LocalDateTime transactionsFetchStartDateTime = LocalDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC);
        boolean tooFarBack = false;

        do {
            TransactionsResponse response = httpServiceV5.getTransactions(olderTransactionsId, context, accountId);
            olderTransactionsId = determineFromId(response);
            List<Transaction> transactions = response.getTransactions();
            if (transactions.isEmpty()) {
                break;
            }

            LocalDateTime lastTransactionDate = LocalDateTime.parse(transactions.get(transactions.size() - 1).getCreated(), FORMATTER);
            if (lastTransactionDate.isBefore(transactionsFetchStartDateTime)) {
                // XXX If we went too far back we have to filter out all transactions older than transactionsFetchStartDateTime
                allTransactions.addAll(transactions.stream()
                        .filter(it -> LocalDateTime.parse(it.getCreated(), FORMATTER).isAfter(transactionsFetchStartDateTime))
                        .collect(Collectors.toList()));
                tooFarBack = true;
            } else {
                allTransactions.addAll(transactions);
            }
        } while ((olderTransactionsId != null) && !tooFarBack);

        return convertTransactionsToDTO(allTransactions);
    }

    private String determineFromId(final PaginatedResponse response) {
        Optional<String> fromIdUrl = Optional.ofNullable(response.getPagination() == null ? null : response.getPagination().getOlderUrl());
        return fromIdUrl.map(it -> it.substring(it.indexOf("id=") + "id=".length())).orElse(null);
    }

    private List<ProviderTransactionDTO> convertTransactionsToDTO(final List<Transaction> transactions) {
        return transactions.stream().map(it ->
                ProviderTransactionDTO.builder()
                        .externalId(it.getTransactionId())
                        .amount(it.getAmount().getValue().abs())
                        .dateTime(dateTimeParser(it.getCreated()))
                        .status(TransactionStatus.BOOKED)
                        .category(YoltCategory.GENERAL)
                        .type(it.getAmount().getValue().compareTo(BigDecimal.ZERO) > 0
                                ? ProviderTransactionType.CREDIT
                                : ProviderTransactionType.DEBIT)
                        .description(it.getDescription())
                        .merchant(it.getMerchant())
                        .extendedTransaction(ExtendedModelMapper.mapToExtendedModelTransaction(it, this::dateTimeParser))
                        .build()
        ).collect(Collectors.toList());
    }

    private ZonedDateTime dateTimeParser(final String dateTime) {
        return ZonedDateTime.from(LocalDateTime.parse(dateTime, FORMATTER).atZone(AMSTERDAM_TIMEZONE));
    }

}
