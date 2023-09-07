package com.yolt.providers.axabanque.common.fetchdata.service;

import com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans;
import com.yolt.providers.axabanque.common.auth.http.clientproducer.HttpClientProducer;
import com.yolt.providers.axabanque.common.consentwindow.ConsentWindow;
import com.yolt.providers.axabanque.common.fetchdata.http.client.FetchDataHttpClient;
import com.yolt.providers.axabanque.common.fetchdata.mapper.AccountMapper;
import com.yolt.providers.axabanque.common.model.external.Account;
import com.yolt.providers.axabanque.common.model.external.Accounts;
import com.yolt.providers.axabanque.common.model.external.Balances;
import com.yolt.providers.axabanque.common.model.external.Transactions;
import com.yolt.providers.axabanque.common.model.external.Transactions.TransactionsMetaData;
import com.yolt.providers.axabanque.common.model.internal.AccessToken;
import com.yolt.providers.axabanque.common.model.internal.FetchDataResult;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.axabanque.common.model.internal.GroupProviderState;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class DefaultFetchDataService implements FetchDataService {
    private final HttpClientProducer restTemplateProvider;
    private final AccountMapper providerAccountMapper;
    private final int transactionPaginationLimit;
    private final ConsentWindow consentWindowUtil;

    @Override
    public DataProviderResponse fetchData(GroupAccessMeans accessMeans, GroupAuthenticationMeans authenticationMeans,
                                          RestTemplateManager restTemplateManager, Instant fetchDataStartTime, String psuIpAddress) throws TokenInvalidException, ProviderFetchDataException {
        FetchDataHttpClient httpClient = restTemplateProvider.getFetchDataHttpClient(authenticationMeans.getTransportKeyId(), authenticationMeans.getTlsCertificate(), restTemplateManager);
        FetchDataResult result = new FetchDataResult();
        GroupProviderState providerState = accessMeans.getProviderState();
        AccessToken accessToken = accessMeans.getAccessToken();
        Accounts accounts = httpClient.getAccounts(accessToken.getToken(), providerState.getConsentId(), providerState.getTraceId(), psuIpAddress);
        List<Account> accountList = accounts != null ? accounts.getAccounts() : Collections.emptyList();
        for (Account account : accountList) {
            try {
                Balances balances = httpClient.getBalances(accessToken.getToken(), providerState.getConsentId(), account.getResourceId(), providerState.getTraceId(), psuIpAddress);
                List<Transactions> transactions = getAllTransactions(httpClient, account.getResourceId(),
                        consentWindowUtil.whenFromToFetchData(providerState.getConsentGeneratedAt(), fetchDataStartTime),
                        accessToken.getToken(), providerState.getConsentId(), providerState.getTraceId(), psuIpAddress);
                result.addFetchedAccount(providerAccountMapper.map(account, balances.getBalances(), transactions));
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(result.getResponseAccounts());
    }

    private List<Transactions> getAllTransactions(FetchDataHttpClient httpClient, String accountId, Instant fetchDataStartTime,
                                                  String accessToken, String consentId, String traceId, String psuIpAddress) throws TokenInvalidException {
        List<Transactions> transactionsList = new ArrayList<>();
        int pageNumber = 0;
        String pageUri = httpClient.getTransactionsEndpoint();
        Transactions transactions = null;
        do {
            transactions = httpClient.getTransactions(accountId, fetchDataStartTime, accessToken, consentId, pageUri, traceId, pageNumber, psuIpAddress);
            transactionsList.add(transactions);
            pageNumber++;
        } while (shouldContinueFetchingTransactions(transactions.getTransactionsMetaData(), pageNumber));
        return transactionsList;
    }

    private boolean shouldContinueFetchingTransactions(TransactionsMetaData transactionsMetaData, int pageNumber) {
        return StringUtils.isNotEmpty(transactionsMetaData.getNext()) && transactionPaginationLimit >= pageNumber;
    }
}

