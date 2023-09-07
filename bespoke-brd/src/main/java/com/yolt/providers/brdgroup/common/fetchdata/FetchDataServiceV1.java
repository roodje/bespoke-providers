package com.yolt.providers.brdgroup.common.fetchdata;

import com.yolt.providers.brdgroup.common.BrdGroupAccessMeans;
import com.yolt.providers.brdgroup.common.config.BrdGroupProperties;
import com.yolt.providers.brdgroup.common.dto.fetchdata.Account;
import com.yolt.providers.brdgroup.common.dto.fetchdata.Balance;
import com.yolt.providers.brdgroup.common.dto.fetchdata.Transaction;
import com.yolt.providers.brdgroup.common.dto.fetchdata.TransactionsResponse;
import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClient;
import com.yolt.providers.brdgroup.common.mapper.AccountMapper;
import com.yolt.providers.brdgroup.common.mapper.TransactionMapper;
import com.yolt.providers.brdgroup.common.util.BrdGroupDateConverter;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class FetchDataServiceV1 implements FetchDataService {

    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final BrdGroupDateConverter dateConverter;
    private final BrdGroupProperties properties;

    @Override
    public DataProviderResponse fetchData(BrdGroupHttpClient brdGroupHttpClient,
                                          BrdGroupAccessMeans brdGroupAccessMeans,
                                          Instant transactionFetchStart,
                                          String psuIpAddress,
                                          String providerDisplayName) throws ProviderFetchDataException {

        List<ProviderAccountDTO> providerAccountsDTO = new ArrayList<>();

        for (Account account : brdGroupHttpClient.getAccounts(brdGroupAccessMeans, psuIpAddress).getAccounts()) {
            String accountId = account.getResourceId();
            try {
                List<Balance> balances = account.getBalances();
                if (CollectionUtils.isEmpty(balances)) {
                    balances = brdGroupHttpClient.getBalances(brdGroupAccessMeans, psuIpAddress, accountId).getBalances();
                }
                List<ProviderTransactionDTO> transactions = new ArrayList<>();

                String dateFrom = dateConverter.toDateFormat(transactionFetchStart);
                TransactionsResponse transactionsResponse = brdGroupHttpClient.getTransactions(brdGroupAccessMeans, accountId, dateFrom, psuIpAddress);
                transactions.addAll(mapTransactions(transactionsResponse.getBookedTransactions(), TransactionStatus.BOOKED));
                transactions.addAll(mapTransactions(transactionsResponse.getPendingTransactions(), TransactionStatus.PENDING));
                int counter = 0;
                String nextPage = transactionsResponse.getNextHref();
                while (shouldFetchNextPage(nextPage, counter)) {
                    transactionsResponse = brdGroupHttpClient.getTransactionsNextPage(nextPage, brdGroupAccessMeans, psuIpAddress);
                    transactions.addAll(mapTransactions(transactionsResponse.getBookedTransactions(), TransactionStatus.BOOKED));
                    transactions.addAll(mapTransactions(transactionsResponse.getPendingTransactions(), TransactionStatus.PENDING));
                    nextPage = transactionsResponse.getNextHref();
                    counter++;
                }

                ProviderAccountDTO providerAccountDTO = accountMapper.map(account, providerDisplayName, balances, transactions);
                providerAccountsDTO.add(providerAccountDTO);
            } catch (Exception e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(providerAccountsDTO);
    }

    private List<ProviderTransactionDTO> mapTransactions(List<Transaction> bankTransactions, TransactionStatus status) {
        return bankTransactions
                .stream()
                .map(t -> transactionMapper.map(t, status))
                .collect(Collectors.toList());
    }

    private boolean shouldFetchNextPage(String nextHref, int counter) {
        return properties.getPaginationLimit() > counter
                && StringUtils.hasText(nextHref);
    }
}
