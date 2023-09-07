package com.yolt.providers.monorepogroup.cecgroup.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupAccessMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.config.CecGroupProperties;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.Account;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.AccountsResponse;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.TransactionsResponse;
import com.yolt.providers.monorepogroup.cecgroup.common.http.CecGroupHttpClient;
import com.yolt.providers.monorepogroup.cecgroup.common.mapper.CecGroupAccountMapper;
import com.yolt.providers.monorepogroup.cecgroup.common.mapper.CecGroupDateConverter;
import com.yolt.providers.monorepogroup.cecgroup.common.mapper.CecGroupTransactionMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CecGroupFetchDataServiceV1 implements CecGroupFetchDataService {

    private final CecGroupAccountMapper accountMapper;
    private final CecGroupTransactionMapper transactionMapper;
    private final CecGroupDateConverter dateConverter;
    private final CecGroupProperties properties;

    @Override
    public DataProviderResponse fetchData(CecGroupAuthenticationMeans authMeans,
                                          CecGroupAccessMeans accessMeans,
                                          CecGroupHttpClient httpClient,
                                          Signer signer,
                                          String psuIpAddress,
                                          Instant transactionsFetchStartTime,
                                          String providerDisplayName) throws ProviderFetchDataException {

        AccountsResponse accountsResponse = httpClient.fetchAccounts(authMeans, accessMeans, signer, psuIpAddress);

        List<ProviderAccountDTO> accounts = new ArrayList<>();
        try {
            for (Account account : accountsResponse.getAccounts()) {

                List<ProviderTransactionDTO> transactions = new ArrayList<>();
                int pageCounter = 1;
                TransactionsResponse transactionsResponse = httpClient.fetchFirstPageOfTransactions(authMeans,
                        accessMeans,
                        signer,
                        psuIpAddress,
                        account.getResourceId(),
                        dateConverter.toIsoDate(transactionsFetchStartTime));

                transactions.addAll(
                        transactionsResponse.getBookedTransactions()
                                .stream()
                                .map(t -> transactionMapper.mapToProviderTransaction(t, TransactionStatus.BOOKED))
                                .collect(Collectors.toList())
                );
                transactions.addAll(
                        transactionsResponse.getPendingTransactions()
                                .stream()
                                .map(t -> transactionMapper.mapToProviderTransaction(t, TransactionStatus.PENDING))
                                .collect(Collectors.toList())
                );

                String nextPageUrl = transactionsResponse.getNextPageUrl();

                while (shouldFetchNextPage(nextPageUrl, pageCounter)) {
                    TransactionsResponse nextPageResponse = httpClient.fetchNextPageOfTransactions(nextPageUrl,
                            authMeans,
                            accessMeans,
                            signer,
                            psuIpAddress);

                    transactions.addAll(
                            nextPageResponse.getBookedTransactions()
                                    .stream()
                                    .map(t -> transactionMapper.mapToProviderTransaction(t, TransactionStatus.BOOKED))
                                    .collect(Collectors.toList())
                    );
                    transactions.addAll(
                            nextPageResponse.getPendingTransactions()
                                    .stream()
                                    .map(t -> transactionMapper.mapToProviderTransaction(t, TransactionStatus.PENDING))
                                    .collect(Collectors.toList())
                    );
                    nextPageUrl = nextPageResponse.getNextPageUrl();
                    pageCounter++;
                }

                ProviderAccountDTO providerAccountDTO = accountMapper.mapToProviderAccount(
                        account,
                        transactions,
                        providerDisplayName
                );
                accounts.add(providerAccountDTO);
            }
            return new DataProviderResponse(accounts);
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private boolean shouldFetchNextPage(String nextPageUrl, int pageCounter) {
        return StringUtils.hasText(nextPageUrl) && pageCounter <= properties.getPaginationLimit();
    }
}
