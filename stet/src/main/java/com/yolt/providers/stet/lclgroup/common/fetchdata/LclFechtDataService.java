package com.yolt.providers.stet.lclgroup.common.fetchdata;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.AccountMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.mapper.transaction.TransactionMapper;
import com.yolt.providers.stet.generic.service.fetchdata.DefaultFetchDataService;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.FetchTransactionsStrategy;

public class LclFechtDataService extends DefaultFetchDataService {

    private static final String ACCOUNTS_ENDPOINT = "/aisp/1.1/accounts";
    private static final String BALANCES_TEMPLATE = "/aisp/1.1/accounts/{accountId}/balances";
    private static final String TRANSACTIONS_TEMPLATE = "/aisp/1.1/accounts/{accountId}/transactions";

    public LclFechtDataService(final FetchDataRestClient restClient,
                               final ProviderStateMapper providerStateMapper,
                               final FetchAccountsStrategy fetchAccountsStrategy,
                               final FetchTransactionsStrategy fetchTransactionsStrategy,
                               final FetchBalancesStrategy fetchBalancesStrategy,
                               final DateTimeSupplier dateMapper,
                               final AccountMapper accountMapper,
                               final TransactionMapper transactionMapper,
                               final DefaultProperties properties) {
        super(restClient,
                providerStateMapper,
                fetchAccountsStrategy,
                fetchTransactionsStrategy,
                fetchBalancesStrategy,
                dateMapper,
                accountMapper,
                transactionMapper,
                properties);
    }

    @Override
    protected String getAccountsEndpoint() {
        return ACCOUNTS_ENDPOINT;
    }

    @Override
    protected String getTransactionsTemplate() {
        return TRANSACTIONS_TEMPLATE;
    }

    @Override
    protected String getBalancesTemplate() {
        return BALANCES_TEMPLATE;
    }

    @Override
    protected boolean shouldIgnoreProcessingAccount(final StetAccountDTO account) {
        return !StetAccountType.CACC.equals(account.getType());
    }
}
