package com.yolt.providers.monorepogroup.qontogroup.common.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoFetchDataResult;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoGroupProviderState;
import com.yolt.providers.monorepogroup.qontogroup.common.http.QontoGroupHttpClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor
public class DefaultQontoFetchDataService implements QontoFetchDataService {

    private final int paginationLimit;
    private final UnaryOperator<List<Account>> accountListFilter;
    private final UnaryOperator<List<Transaction>> transactionListFilter;

    @Override
    public QontoFetchDataResult fetchAccount(QontoGroupAuthenticationMeans authenticationMeans, QontoGroupHttpClient httpClient, QontoGroupProviderState providerState, Instant transactionsFetchStartTime, String psuIpAddress, Signer signer) throws TokenInvalidException {
        var fetchDataResult = new QontoFetchDataResult();
        var organization = httpClient.fetchOrganization(providerState.getAccessToken(), psuIpAddress, signer, authenticationMeans.getSigningData());
        for (Account account : accountListFilter.apply(organization.getAccounts())) {
            String currentPage = "1";
            int pageCounter = 1;
            while (StringUtils.isNotEmpty(currentPage) && pageCounter < paginationLimit) {
                var transactions = httpClient.fetchTransactions(providerState.getAccessToken(),
                        psuIpAddress,
                        signer,
                        authenticationMeans.getSigningData(),
                        account.getIban(),
                        transactionsFetchStartTime,
                        currentPage);
                fetchDataResult.addResources(account, transactionListFilter.apply(transactions.getTransactions()));
                currentPage = transactions.getNextPage();
                pageCounter++;
            }
        }
        return fetchDataResult;
    }
}
