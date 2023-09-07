package com.yolt.providers.abnamrogroup.common.data;

import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamro.dto.*;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class AbnAmroFetchDataService {

    private static final String CONSENT_INFO_URI = "/v1/consentinfo";
    private static final String ACCOUNT_DETAILS_URI = "/v1/accounts/%s/details";
    private static final String BALANCE_URI = "/v1/accounts/%s/balances";
    private static final String TRANSACTIONS_URI = "/v1/accounts/%s/transactions";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final int paginationLimit;
    private final AbnAmroDataMapper dataMapper;

    public List<ProviderAccountDTO> fetchAccounts(final RestTemplate restTemplate, final AccessTokenResponseDTO accessToken,
                                                  final Instant transactionFetchStartTime, final String apiKey) throws TokenInvalidException {

        List<ProviderAccountDTO> providerAccountList = new ArrayList<>();
        try {
            ConsentInfoResponse consentInfo = getConsentInfo(restTemplate, accessToken, apiKey);
            if (consentInfo != null && StringUtils.isNotBlank(consentInfo.getIban())) {
                final String iban = consentInfo.getIban();
                DetailsResponse accountDetails = getAccountDetails(restTemplate, accessToken, apiKey, iban);
                BalanceResponse balanceResponse = getBalance(restTemplate, accessToken, apiKey, iban);
                List<TransactionResponseTransactions> transactions = collectAllTransactions(restTemplate, accessToken,
                        apiKey, iban, transactionFetchStartTime);

                ProviderAccountDTO providerAccountDTO = dataMapper.convertAccount(accountDetails, balanceResponse, transactions);
                if (providerAccountDTO != null) {
                    providerAccountList.add(providerAccountDTO);
                }
            }
        } catch (HttpStatusCodeException e) {
            if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode()) && e.getMessage() != null
                    && e.getMessage().contains("The presented access token is not valid or expired")) {
                throw new TokenInvalidException("Token was invalid or expired.");
            }
            if (HttpStatus.TOO_MANY_REQUESTS.equals(e.getStatusCode())) {
                throw new BackPressureRequestException(e.getMessage());
            }
            throw e;
        }
        return providerAccountList;
    }

    private ConsentInfoResponse getConsentInfo(final RestTemplate restTemplate, final AccessTokenResponseDTO accessToken, final String apiKey) {
        return restTemplate.exchange(CONSENT_INFO_URI, HttpMethod.GET,
                new HttpEntity<>(defaultHttpHeaders(accessToken, apiKey)), ConsentInfoResponse.class).getBody();
    }

    private DetailsResponse getAccountDetails(final RestTemplate restTemplate, final AccessTokenResponseDTO accessToken,
                                              final String apiKey, final String iban) {
        return restTemplate.exchange(String.format(ACCOUNT_DETAILS_URI, iban), HttpMethod.GET,
                new HttpEntity<>(defaultHttpHeaders(accessToken, apiKey)), DetailsResponse.class).getBody();
    }

    private BalanceResponse getBalance(final RestTemplate restTemplate, final AccessTokenResponseDTO accessToken,
                                       final String apiKey, final String iban) {
        return restTemplate.exchange(String.format(BALANCE_URI, iban), HttpMethod.GET,
                new HttpEntity<>(defaultHttpHeaders(accessToken, apiKey)), BalanceResponse.class).getBody();
    }

    /**
     * Transaction history retrieved is at most 18 months old.
     * Every page returns at most 50 transactions.
     * <p>
     * Pagination for transactions works a bit differently from what is specified in ABN AMRO API spec:
     * - when original transaction request does not have any query parameters, only 'nextPageKey' parameter
     * should be specified to obtain next page of transactions
     * - when original transaction request has query parameters like 'bookDateFrom' or 'bookDateTo', all those
     * parameters together with 'nextPageKey' should be specified to obtain next page of transactions
     */
    private List<TransactionResponseTransactions> collectAllTransactions(final RestTemplate restTemplate,
                                                                         final AccessTokenResponseDTO accessToken,
                                                                         final String apiKey,
                                                                         final String iban,
                                                                         final Instant transactionFetchStartTime) {
        List<TransactionResponseTransactions> transactionsList = new ArrayList<>();
        String dateFromParam = transactionFetchStartTime == null ? null
                : "bookDateFrom=" + DATE_FORMATTER.format(OffsetDateTime.ofInstant(transactionFetchStartTime, ZoneOffset.UTC));
        TransactionResponse response = getTransactions(restTemplate, accessToken, apiKey, iban, dateFromParam);

        if (response != null && response.getTransactions() != null) {
            transactionsList.addAll(response.getTransactions());
            String nextPageKey = response.getNextPageKey();
            int retrievedPagesCount = 1; // retrieved 1st response above
            while (StringUtils.isNotBlank(nextPageKey) && retrievedPagesCount < paginationLimit) {
                String nextPageParam = "nextPageKey=" + nextPageKey;
                TransactionResponse innerResponse;
                if (StringUtils.isNotBlank(dateFromParam)) {
                    innerResponse = getTransactions(restTemplate, accessToken, apiKey, iban, dateFromParam + "&" + nextPageParam);
                } else {
                    innerResponse = getTransactions(restTemplate, accessToken, apiKey, iban, nextPageParam);
                }

                if (innerResponse != null && innerResponse.getTransactions() != null) {
                    transactionsList.addAll(innerResponse.getTransactions());
                    nextPageKey = innerResponse.getNextPageKey();
                } else {
                    nextPageKey = null;
                }
                retrievedPagesCount++;
            }
        }

        return transactionsList;
    }

    private TransactionResponse getTransactions(final RestTemplate restTemplate, final AccessTokenResponseDTO accessToken,
                                                final String apiKey, final String iban, final String queryParams) {
        String requestUri = String.format(TRANSACTIONS_URI, iban);
        if (StringUtils.isNotBlank(queryParams)) {
            requestUri += "?" + queryParams;
        }
        return restTemplate.exchange(requestUri, HttpMethod.GET, new HttpEntity<>(defaultHttpHeaders(accessToken, apiKey)),
                TransactionResponse.class).getBody();
    }

    private static HttpHeaders defaultHttpHeaders(final AccessTokenResponseDTO accessToken, final String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.setBearerAuth(accessToken.getAccessToken());
        headers.set("API-Key", apiKey);
        return headers;
    }
}
