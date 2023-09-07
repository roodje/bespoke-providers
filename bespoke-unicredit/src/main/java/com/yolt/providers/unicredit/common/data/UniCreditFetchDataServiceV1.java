package com.yolt.providers.unicredit.common.data;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.JsonParseException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans;
import com.yolt.providers.unicredit.common.config.UniCreditBaseProperties;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapper;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditDataMapper;
import com.yolt.providers.unicredit.common.data.transformer.ProviderStateTransformer;
import com.yolt.providers.unicredit.common.dto.*;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClient;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.yolt.providers.unicredit.common.rest.UniCreditHttpClient.TRANSACTION_ENDPOINT;

@RequiredArgsConstructor
public class UniCreditFetchDataServiceV1 implements UniCreditFetchDataService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UniCreditHttpClientFactory httpClientFactory;
    private final ProviderStateTransformer<UniCreditAccessMeansDTO> stateTransformer;
    private final UniCreditBaseProperties properties;
    private final UniCreditDataMapper dataMapper;
    private final UniCreditAuthMeansMapper authMeansMapper;

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData, ProviderInfo providerInfo) throws ProviderFetchDataException {
        UniCreditAuthMeans authMeans = authMeansMapper.fromBasicAuthenticationMeans(urlFetchData.getAuthenticationMeans(), providerInfo.getIdentifier());
        UniCreditAccessMeansDTO accessMeansDTO = stateTransformer.transformToObject(urlFetchData.getAccessMeans().getAccessMeans());
        UniCreditHttpClient httpClient = httpClientFactory.createHttpClient(authMeans, urlFetchData.getRestTemplateManager(), providerInfo.getDisplayName(), properties.getBaseUrl());
        return getAccountAndTransactions(httpClient, urlFetchData.getPsuIpAddress(), accessMeansDTO.getConsentId(), urlFetchData.getTransactionsFetchStartTime());
    }

    private DataProviderResponse getAccountAndTransactions(final UniCreditHttpClient httpClient,
                                                           final String psuIpAddress,
                                                           final String consentId,
                                                           final Instant dateFrom) throws ProviderFetchDataException {
        List<ProviderAccountDTO> mappedAccounts = new ArrayList<>();
        String currentPage = UniCreditHttpClient.ACCOUNTS_ENDPOINT;
        String nextPage = null;
        int pageCounter = 1;
        do {
            try {
                UniCreditAccountsDTO accounts = httpClient.getAccounts(currentPage, consentId, psuIpAddress);
                for (UniCreditAccountDTO account : accounts.getAccounts()) {
                    retrieveAndMapAccountData(account, consentId, httpClient, mappedAccounts, psuIpAddress, dateFrom);
                    nextPage = account.getNextPageUrl();
                }
            } catch (JsonParseException | RestClientException | MissingDataException | TokenInvalidException e) {
                throw new ProviderFetchDataException(e);
            }
            // Prevent infinite loop on failure to get nextPage
            // Failed Account will already be added because an exception will be thrown and caught during 'performRequest()' call.
            if (Objects.equals(currentPage, nextPage)) {
                break;
            }
            currentPage = nextPage;
            pageCounter++;
        } while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());

        return new DataProviderResponse(mappedAccounts);
    }

    private void retrieveAndMapAccountData(final UniCreditAccountDTO account,
                                           final String consentId,
                                           final UniCreditHttpClient httpClient,
                                           final List<ProviderAccountDTO> mappedAccounts,
                                           final String psuIpAddress,
                                           final Instant dateFrom) throws TokenInvalidException {
        if (dataMapper.verifyAccountType(account.getCashAccountType())) {
            List<UniCreditTransactionsDTO> transactions = getTransactions(httpClient, account.getResourceId(), consentId, dateFrom, psuIpAddress);
            List<UniCreditBalanceDTO> balances = account.getBalances();
            if (balances == null || balances.isEmpty()) {
                balances = getBalances(httpClient, account.getResourceId(), consentId, psuIpAddress);
            }

            ProviderAccountDTO mappedAccount = dataMapper.mapToAccount(account, transactions, balances);
            mappedAccounts.add(mappedAccount);
        }
    }

    private List<UniCreditTransactionsDTO> getTransactions(final UniCreditHttpClient httpClient,
                                                           final String accountId,
                                                           final String consentId,
                                                           final Instant dateFrom,
                                                           final String psuIpAddress) throws TokenInvalidException {
        List<UniCreditTransactionsDTO> transactions = new ArrayList<>();
        String formattedDateFrom = DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(dateFrom, ZoneId.of("Europe/Rome")));
        String nextPage = String.format(TRANSACTION_ENDPOINT, accountId, formattedDateFrom);
        int pageCounter = 1;
        do {
            UniCreditTransactionsDTO transactionsResponse = httpClient.getTransactions(nextPage, consentId, psuIpAddress);
            transactions.add(transactionsResponse);

            // Prevent infinite loop on failure to get nextPage
            if (nextPage.equalsIgnoreCase(transactionsResponse.getNextPageUrl()))
                break;

            nextPage = transactionsResponse.getNextPageUrl();
            pageCounter++;
        } while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());
        return transactions;
    }

    private List<UniCreditBalanceDTO> getBalances(final UniCreditHttpClient httpClient,
                                                  final String accountId,
                                                  final String consentId,
                                                  final String psuIpAddress) throws TokenInvalidException {
        UniCreditBalancesDTO balances = httpClient.getBalances(String.format(UniCreditHttpClient.BALANCES_ENDPOINT, accountId), consentId, psuIpAddress);
        if (balances == null) {
            throw new MissingDataException("Retrieved balances are null.");
        }
        return balances.getBalances();
    }
}
