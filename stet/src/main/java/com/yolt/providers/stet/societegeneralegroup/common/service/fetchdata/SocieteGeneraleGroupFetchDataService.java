package com.yolt.providers.stet.societegeneralegroup.common.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.AccountMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.mapper.transaction.TransactionMapper;
import com.yolt.providers.stet.generic.service.fetchdata.DefaultFetchDataService;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.request.FetchDataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.FetchTransactionsStrategy;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class SocieteGeneraleGroupFetchDataService extends DefaultFetchDataService {

    public SocieteGeneraleGroupFetchDataService(FetchDataRestClient restClient,
                                                ProviderStateMapper providerStateMapper,
                                                FetchAccountsStrategy fetchAccountsStrategy,
                                                FetchTransactionsStrategy fetchTransactionsStrategy,
                                                FetchBalancesStrategy fetchBalancesStrategy,
                                                DateTimeSupplier dateMapper,
                                                AccountMapper accountMapper,
                                                TransactionMapper transactionMapper,
                                                DefaultProperties properties) {
        super(restClient, providerStateMapper, fetchAccountsStrategy, fetchTransactionsStrategy, fetchBalancesStrategy, dateMapper, accountMapper, transactionMapper, properties);
    }

    @Override
    public DataProviderResponse getAccountsAndTransactions(HttpClient httpClient,
                                                           FetchDataRequest fetchDataRequest) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

        DataRequest dataRequest = new DataRequest(fetchDataRequest);
        try {
            List<StetAccountDTO> accounts = fetchAccountsStrategy.fetchAccounts(
                    httpClient,
                    getAccountsEndpoint(),
                    getConsentsEndpoint(),
                    dataRequest);

            for (StetAccountDTO account : accounts) {
                if (shouldIgnoreProcessingAccount(account)) {
                    continue;
                }

                List<StetBalanceDTO> balances = account.getBalances();

                List<StetTransactionDTO> transactions = fetchTransactionsStrategy.fetchTransactions(
                        httpClient,
                        account.getTransactionsUrl(),
                        dataRequest,
                        fetchDataRequest.getTransactionsFetchStartTime());

                List<ProviderTransactionDTO> transactionDTOs = transactionMapper.mapToProviderTransactionDTOs(transactions);
                ProviderAccountDTO accountDTO = accountMapper.mapToProviderAccountDTO(account, balances, transactionDTOs);
                responseAccounts.add(accountDTO);
            }
        } catch (ProviderHttpStatusException | HttpStatusCodeException e) {
            throw new ProviderFetchDataException(e);
        }
        return new DataProviderResponse(Collections.unmodifiableList(responseAccounts));
    }

    @Override
    protected boolean shouldIgnoreProcessingAccount(StetAccountDTO account) {
        return account.getTransactionsUrl() == null;
    }
}
