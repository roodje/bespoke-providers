package com.yolt.providers.amexgroup.common.service;

import com.yolt.providers.amex.common.dto.*;
import com.yolt.providers.amexgroup.common.AmexGroupConfigurationProperties;
import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeans;
import com.yolt.providers.amexgroup.common.dto.TokenResponse;
import com.yolt.providers.amexgroup.common.dto.TokenResponses;
import com.yolt.providers.amexgroup.common.exception.AmexGroupMalformedDataException;
import com.yolt.providers.amexgroup.common.mapper.AmexGroupDataMapper;
import com.yolt.providers.amexgroup.common.utils.AmexDateTimeUtils;
import com.yolt.providers.amexgroup.common.utils.AmexMacHeaderUtilsV2;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.JsonParseException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@RequiredArgsConstructor
public class AmexGroupFetchDataServiceV5 implements AmexGroupFetchDataService {

    private static final String GET_ACCOUNTS_ENDPOINT = "/servicing/v1/member/accounts";
    private static final String GET_TRANSACTIONS_ENDPOINT = "/servicing/v1/financials/transactions";
    private static final String GET_BALANCES_ENDPOINT = "/servicing/v1/financials/balances";
    private static final String GET_STATEMENT_PERIODS_ENDPOINT = "/servicing/v1/financials/statement_periods";
    private static final Object MERCHANT = "merchant";
    private static final String EXTENDED_DETAILS = "extended_details";
    private static final int TWO_MONTHS_EXTENSION = 62;

    private final AmexGroupRestTemplateServiceV3 amexGroupRestTemplateService;
    private final AmexGroupConfigurationProperties amexGroupConfigurationProperties;
    private final AmexMacHeaderUtilsV2 amexMacHeaderUtils;
    private final AmexGroupDataMapper dataMapper;

    @Override
    public DataProviderResponse getAccountsAndTransactions(AmexGroupAuthMeans amexGroupAuthMeans,
                                                           RestTemplateManager restTemplateManager,
                                                           TokenResponses tokenResponses,
                                                           Instant requestedTransactionsFetchStartTime) throws ProviderFetchDataException, TokenInvalidException {
        RestTemplate restTemplate = amexGroupRestTemplateService.buildRestTemplate(amexGroupAuthMeans, restTemplateManager);
        List<ProviderAccountDTO> accounts = new ArrayList<>();
        for (TokenResponse tokenResponse : tokenResponses.getTokens()) {
            try {
                Account account = getAccount(restTemplate, tokenResponse, amexGroupAuthMeans);

                // C4PO-8230 skipping supplementary accounts as not supporting balances
                if (shouldSkipAccount(account)) continue;

                List<Balance> balances = getBalances(restTemplate, tokenResponse, amexGroupAuthMeans);
                Transactions pendingTransactions = getPendingTransactions(restTemplate, tokenResponse, amexGroupAuthMeans);
                filterTransactionsByRequestedTransactionsFetchStartTime(pendingTransactions.getTransactions(), requestedTransactionsFetchStartTime);
                Transactions transactions = getTransactionsByStatementPeriods(
                        restTemplate,
                        requestedTransactionsFetchStartTime,
                        tokenResponse,
                        amexGroupAuthMeans);
                filterTransactionsByRequestedTransactionsFetchStartTime(transactions.getTransactions(), requestedTransactionsFetchStartTime);
                ProviderAccountDTO providerAccountDTO = dataMapper.mapToAccount(account, balances, transactions, pendingTransactions);
                accounts.add(providerAccountDTO);
            } catch (HttpStatusCodeException e) {
                if (isTokenInvalidException(e)) {
                    throw new TokenInvalidException(String.format("Token invalid, received status %s.", e.getStatusCode()));
                }
                throw new ProviderFetchDataException(e);
            } catch (RestClientException | MissingDataException | JsonParseException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(accounts);
    }

    private boolean shouldSkipAccount(Account account) {
        return !account.getIdentifiers().getIsBasic();
    }

    private void filterTransactionsByRequestedTransactionsFetchStartTime(List<Transaction> transactions,
                                                                         Instant requestedTransactionsFetchStartTime) {
        transactions.removeIf(transaction -> AmexDateTimeUtils.getInstant(transaction.getChargeDate()).isBefore(requestedTransactionsFetchStartTime));
    }

    private Transactions getTransactionsByStatementPeriods(RestTemplate restTemplate,
                                                           Instant requestedTransactionsFetchStartTime,
                                                           TokenResponse tokenResponse,
                                                           AmexGroupAuthMeans amexGroupAuthMeans) throws ProviderFetchDataException {
        Transactions transactions = new Transactions();
        transactions.setTransactions(new ArrayList<>());
        StatementPeriods periods = narrowPeriodsToRequestedStartDate(
                getStatementPeriods(restTemplate, tokenResponse, amexGroupAuthMeans),
                requestedTransactionsFetchStartTime);

        for (StatementPeriod period : periods.getStatementPeriods()) {
            transactions.getTransactions().addAll(getTransactionsFromPeriod(
                    restTemplate,
                    period.getEndDate(),
                    tokenResponse,
                    amexGroupAuthMeans).getTransactions());
        }
        return transactions;
    }

    private Transactions getTransactionsFromPeriod(RestTemplate restTemplate,
                                                   String periodEndDate,
                                                   TokenResponse tokenResponse,
                                                   AmexGroupAuthMeans amexGroupAuthMeans) throws ProviderFetchDataException {
        int totalTransactionsCount = 0;
        Transactions transactions = new Transactions();
        transactions.setTransactions(new ArrayList<>());
        for (int pageNumber = 0; isPageLimitOrTransactionLimitReached(totalTransactionsCount, pageNumber);
             pageNumber++) {
            List<Transactions> transactionsResponse = getTransactionsPageByStatementPeriod(
                    restTemplate,
                    periodEndDate,
                    tokenResponse,
                    amexGroupAuthMeans,
                    pageNumber);
            if (validateTransactions(transactionsResponse)) {
                int newTotalTransactionsCount = Integer.parseInt(transactionsResponse.get(0).getTotalTransactionCount());
                if (pageNumber > 0 && newTotalTransactionsCount != totalTransactionsCount) {
                    //https://yolt.atlassian.net/browse/C4PO-3863
                    //It shouldn't happen, but if it does, we want to know
                    log.warn("Total booked transactions count differs between pagination calls!");
                }
                totalTransactionsCount = newTotalTransactionsCount;
                transactions.getTransactions().addAll(transactionsResponse.get(0).getTransactions());
            } else {
                throwMalformedTransactionsReceived(transactionsResponse);
            }
        }
        return transactions;
    }

    private boolean isPageLimitOrTransactionLimitReached(Integer totalTransactionsCount, int pageNumber) {
        return pageNumber == 0 || pageNumber <= amexGroupConfigurationProperties.getPaginationLimit() &&
                (pageNumber * amexGroupConfigurationProperties.getPageSize() < totalTransactionsCount);
    }

    private List<Transactions> getTransactionsPageByStatementPeriod(RestTemplate restTemplate,
                                                                    String statementEndDate,
                                                                    TokenResponse tokenResponse,
                                                                    AmexGroupAuthMeans amexGroupAuthMeans,
                                                                    int pageNumber) throws ProviderFetchDataException {
        HttpEntity httpEntity = new HttpEntity<>(createDefaultHeaders(tokenResponse, amexGroupAuthMeans, GET_TRANSACTIONS_ENDPOINT));
        String url = UriComponentsBuilder.fromUriString(GET_TRANSACTIONS_ENDPOINT)
                .queryParam(EXTENDED_DETAILS, MERCHANT)
                .queryParam("statement_end_date", statementEndDate)
                .queryParam("limit", amexGroupConfigurationProperties.getPageSize())
                .queryParam("offset", pageNumber * amexGroupConfigurationProperties.getPageSize())
                .toUriString();
        return restTemplate.exchange(url, GET, httpEntity, new ParameterizedTypeReference<List<Transactions>>() {
        }).getBody();
    }

    private StatementPeriods getStatementPeriods(RestTemplate restTemplate,
                                                 TokenResponse tokenResponse,
                                                 AmexGroupAuthMeans amexGroupAuthMeans) throws ProviderFetchDataException {
        HttpEntity httpEntity = new HttpEntity<>(createDefaultHeaders(tokenResponse, amexGroupAuthMeans, GET_STATEMENT_PERIODS_ENDPOINT));
        return restTemplate.exchange(GET_STATEMENT_PERIODS_ENDPOINT, GET, httpEntity, StatementPeriods.class).getBody();
    }

    /**
     * Narrows given periods to these which fit requested date (RD) + two periods (2 Months).
     * transaction from the given period can have date before that period, so we request about
     * 2 periods more and filter it later to fit requestedTransactionsFetchStartTime.
     * It compares periods' end date being after RD.
     * Second corner case is end date being null.
     * Third corner case is short RD - when SM asks us for RD=01.05.2020
     * and the statement periods are:
     * {"statement_periods":[
     * {"start_date":"2020-04-16","end_date":"2020-05-15","index":0},
     * {"start_date":"2020-03-16","end_date":"2020-04-15","index":1},
     * {"start_date":"2020-02-16","end_date":"2020-03-15","index":2},
     * {"start_date":"2020-01-16","end_date":"2020-02-15","index":3},
     * {"start_date":"2019-12-16","end_date":"2020-01-15","index":4},
     * {"start_date":"2019-11-17","end_date":"2019-12-15","index":5}
     * ]}
     * then we use index field to get at least the most fresh period (even if
     * its start date would be before RD)
     */
    private StatementPeriods narrowPeriodsToRequestedStartDate(StatementPeriods statementPeriods, Instant requestedTransactionsFetchStartTime) {
        Instant extendedTransactionsFetchStartTime = requestedTransactionsFetchStartTime.minus(Period.ofDays(TWO_MONTHS_EXTENSION));
        StatementPeriods narrowedPeriods = new StatementPeriods();
        for (StatementPeriod period : statementPeriods.getStatementPeriods()) {
            if (Objects.isNull(period.getEndDate()) || period.getIndex() == 0 ||
                    AmexDateTimeUtils.getInstant(period.getEndDate()).isAfter(extendedTransactionsFetchStartTime)) {
                narrowedPeriods.addStatementPeriodsItem(period);
            }
        }
        return narrowedPeriods;
    }

    private Transactions getPendingTransactions(RestTemplate restTemplate,
                                                TokenResponse tokenResponse,
                                                AmexGroupAuthMeans amexGroupAuthMeans) throws ProviderFetchDataException {
        int totalTransactionsCount = 0;
        Transactions transactions = new Transactions();
        transactions.setTransactions(new ArrayList<>());
        for (int pageNumber = 0; isPageLimitOrTransactionLimitReached(totalTransactionsCount, pageNumber);
             pageNumber++) {
            List<Transactions> transactionsResponse = getPendingTransactionsPage(
                    restTemplate,
                    tokenResponse,
                    amexGroupAuthMeans,
                    pageNumber);

            if (validateTransactions(transactionsResponse)) {
                int newTotalTransactionsCount = Integer.parseInt(transactionsResponse.get(0).getTotalTransactionCount());
                if (pageNumber > 0 && newTotalTransactionsCount != totalTransactionsCount) {
                    //https://yolt.atlassian.net/browse/C4PO-3863
                    //It shouldn't have happen, but if it does, we want to know
                    log.warn("Total pending transactions count differs between pagination calls!");
                }
                totalTransactionsCount = newTotalTransactionsCount;
                transactions.getTransactions().addAll(transactionsResponse.get(0).getTransactions());
            } else {
                throwMalformedTransactionsReceived(transactionsResponse);
            }
        }
        return transactions;
    }

    private void throwMalformedTransactionsReceived(List<Transactions> transactionsResponse) {
        throw new AmexGroupMalformedDataException(String.format("Expected 1 transactions object in the list, %s",
                (transactionsResponse == null) ? "NULL received" : String.format(" received %d", transactionsResponse.size())));
    }

    private List<Transactions> getPendingTransactionsPage(RestTemplate restTemplate,
                                                          TokenResponse tokenResponse,
                                                          AmexGroupAuthMeans amexGroupAuthMeans,
                                                          int pageNumber) throws ProviderFetchDataException {
        HttpEntity httpEntity = new HttpEntity<>(createDefaultHeaders(tokenResponse, amexGroupAuthMeans, GET_TRANSACTIONS_ENDPOINT));
        String url = UriComponentsBuilder.fromUriString(GET_TRANSACTIONS_ENDPOINT)
                .queryParam(EXTENDED_DETAILS, MERCHANT)
                .queryParam("status", "pending")
                .queryParam("limit", amexGroupConfigurationProperties.getPageSize())
                .queryParam("offset", pageNumber * amexGroupConfigurationProperties.getPageSize())
                .toUriString();
        return restTemplate.exchange(url, GET, httpEntity, new ParameterizedTypeReference<List<Transactions>>() {
        }).getBody();
    }

    private Account getAccount(RestTemplate restTemplate,
                               TokenResponse tokenResponse,
                               AmexGroupAuthMeans amexGroupAuthMeans) throws ProviderFetchDataException {
        HttpEntity httpEntity = new HttpEntity(createDefaultHeaders(tokenResponse, amexGroupAuthMeans, GET_ACCOUNTS_ENDPOINT));
        return restTemplate.exchange(GET_ACCOUNTS_ENDPOINT, GET, httpEntity, Account.class).getBody();
    }

    private boolean validateTransactions(List<Transactions> transactions) {
        return transactions != null && transactions.size() == 1;
    }

    private List<Balance> getBalances(RestTemplate restTemplate,
                                      TokenResponse tokenResponse,
                                      AmexGroupAuthMeans amexGroupAuthMeans) throws ProviderFetchDataException {
        HttpEntity httpEntity = new HttpEntity<>(createDefaultHeaders(tokenResponse, amexGroupAuthMeans, GET_BALANCES_ENDPOINT));
        return restTemplate.exchange(GET_BALANCES_ENDPOINT, GET, httpEntity,
                new ParameterizedTypeReference<List<Balance>>() {
                }).getBody();
    }

    private HttpHeaders createDefaultHeaders(TokenResponse tokenResponse,
                                             AmexGroupAuthMeans accessMeans,
                                             String resourcePath) throws ProviderFetchDataException {
        String macToken;
        try {
            macToken = amexMacHeaderUtils.generateDataMacToken(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getMacKey(),
                    GET.name(),
                    amexGroupConfigurationProperties.getHost(),
                    amexGroupConfigurationProperties.getPort(),
                    resourcePath);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw new ProviderFetchDataException("Cannot create MAC token for AmEx");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, macToken);
        headers.add("x-amex-api-key", accessMeans.getClientId());
        return headers;
    }

    private boolean isTokenInvalidException(HttpStatusCodeException e) {
        return UNAUTHORIZED.equals(e.getStatusCode()) || FORBIDDEN.equals(e.getStatusCode());
    }
}