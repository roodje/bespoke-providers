package com.yolt.providers.belfius.common.service;

import com.yolt.providers.belfius.common.http.client.BelfiusGroupHttpClient;
import com.yolt.providers.belfius.common.model.BelfiusGroupAccessMeans;
import com.yolt.providers.belfius.common.model.ais.Account;
import com.yolt.providers.belfius.common.model.ais.TransactionResponse;
import com.yolt.providers.belfius.common.service.mapper.BelfiusGroupMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class BelfiusGroupFetchDataService {

    private final BelfiusGroupMapper mapper;

    public DataProviderResponse fetchData(BelfiusGroupHttpClient httpClient, BelfiusGroupAccessMeans accessMeans, Instant fromDate) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> accounts = new ArrayList<>();
        String logicalId = accessMeans.getAccessToken().getLogicalId();
        String accessToken = accessMeans.getAccessToken().getAccessToken();

        try {
            Account account = httpClient.getAccountForGivenLogicalId(logicalId, accessToken);
            List<TransactionResponse.Transaction> allTransactionsForGivenAccount = new ArrayList<>();

            TransactionResponse transactions = httpClient.getTransactionsForGivenLogicalId(logicalId, accessToken, fromDate);
            allTransactionsForGivenAccount.addAll(transactions.getTransactions());

            while (fetchingAnotherTransactionIsPossible(transactions)) {
                transactions = httpClient.getTransactionsNextPageForGivenLogicalId(logicalId, accessToken, transactions.getNextPageUrl());
                allTransactionsForGivenAccount.addAll(transactions.getTransactions());
            }

            accounts.add(mapper.mapToProviderAccountDTO(account, allTransactionsForGivenAccount, logicalId));
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
        return new DataProviderResponse(accounts);
    }

    private boolean fetchingAnotherTransactionIsPossible(TransactionResponse transactions) {
        return !CollectionUtils.isEmpty(transactions.getTransactions()) && StringUtils.isNotEmpty(transactions.getNextPageUrl());
    }
}
