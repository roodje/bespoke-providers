package com.yolt.providers.monorepogroup.chebancagroup.common.service;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.chebancagroup.common.http.CheBancaGroupHttpClient;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.CheBancaGroupTokenMapper;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class DefaultChebancaGroupFetchDataService implements ChebancaGroupFetchDataService {
    private final CheBancaGroupTokenMapper tokenMapper;
    private final int paginationLimit;

    @Override
    public FetchDataResult fetchData(final UrlFetchDataRequest fetchDataRequest, final Signer signer, final CheBancaGroupHttpClient httpClient, final CheBancaGroupAuthenticationMeans authenticationMeans, final Instant transactionsFetchStartTime) throws TokenInvalidException, ProviderFetchDataException {
        try {
            FetchDataResult result = new FetchDataResult();
            var accessMeans = tokenMapper.mapToToken(fetchDataRequest.getAccessMeans());
            var customerIdResponse = httpClient.getCustomerId(signer, authenticationMeans, accessMeans.getAccessToken());

            var accountsResponse = httpClient.fetchAccounts(
                    fetchDataRequest.getSigner(),
                    authenticationMeans,
                    accessMeans.getAccessToken(),
                    customerIdResponse.getCustomerId());


            for (Account account : accountsResponse.getAccounts()) {
                var transactionResponse = httpClient.fetchTransactions(
                        fetchDataRequest.getSigner(),
                        authenticationMeans,
                        accessMeans.getAccessToken(),
                        customerIdResponse.getCustomerId(),
                        account.getProductCode(),
                        transactionsFetchStartTime
                );
                result.addTransactions(account, transactionResponse.getBookedTransactions(), transactionResponse.getPendingTransactions());

                int fetchedPages = 1;
                String nextPageUrl = transactionResponse.getNextHref();
                while (StringUtils.isNotEmpty(nextPageUrl) && fetchedPages < paginationLimit) {
                    var nextTransactionResponse =
                            httpClient.fetchTransactions(
                                    fetchDataRequest.getSigner(),
                                    authenticationMeans,
                                    accessMeans.getAccessToken(),
                                    nextPageUrl);
                    result.addTransactions(account, nextTransactionResponse.getBookedTransactions(), nextTransactionResponse.getPendingTransactions());

                    fetchedPages++;
                    if (!nextPageUrl.equals(nextTransactionResponse.getNextHref())) {
                        nextPageUrl = nextTransactionResponse.getNextHref();
                    } else {
                        nextPageUrl = null;
                    }
                }

                var balance = httpClient.fetchBalances(
                        fetchDataRequest.getSigner(),
                        authenticationMeans,
                        accessMeans.getAccessToken(),
                        customerIdResponse.getCustomerId(),
                        account.getProductCode()
                );
                result.addBalance(account, balance);
            }
            return result;
        } catch (TokenInvalidException e) {
            throw new ProviderFetchDataException(e);
        }
    }
}
