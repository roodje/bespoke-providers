package com.yolt.providers.stet.cicgroup.cic.service.fetchdata;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.AccountMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.mapper.transaction.TransactionMapper;
import com.yolt.providers.stet.generic.service.fetchdata.DefaultFetchDataService;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.FetchTransactionsStrategy;


public class CicFetchDataService extends DefaultFetchDataService {

    protected static final String ACCOUNTS_ENDPOINT = "/cic/stet-psd2-api/v2.1/accounts";
    protected static final String BALANCES_TEMPLATE = "/cic/stet-psd2-api/v2.1/accounts/{accountId}/balances";
    protected static final String TRANSACTIONS_TEMPLATE = "/cic/stet-psd2-api/v2.1/accounts/{accountId}/transactions";

    public CicFetchDataService(FetchDataRestClient restClient, ProviderStateMapper providerStateMapper, FetchAccountsStrategy fetchAccountsStrategy, FetchTransactionsStrategy fetchTransactionsStrategy, FetchBalancesStrategy fetchBalancesStrategy, DateTimeSupplier dateMapper, AccountMapper accountMapper, TransactionMapper transactionMapper, DefaultProperties properties) {
        super(restClient, providerStateMapper, fetchAccountsStrategy, fetchTransactionsStrategy, fetchBalancesStrategy, dateMapper, accountMapper, transactionMapper, properties);
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
    protected String getAccountsEndpoint() {
        return ACCOUNTS_ENDPOINT;
    }
}
