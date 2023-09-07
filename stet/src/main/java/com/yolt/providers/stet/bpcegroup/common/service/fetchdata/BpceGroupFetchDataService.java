package com.yolt.providers.stet.bpcegroup.common.service.fetchdata;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BpceGroupFetchDataService extends DefaultFetchDataService {

    protected static final String ACCOUNTS_ENDPOINT = "/stet/psd2/v1.4.2/accounts";
    protected static final String BALANCES_TEMPLATE = "/stet/psd2/v1.4.2/accounts/{accountId}/balances";
    protected static final String TRANSACTIONS_TEMPLATE = "/stet/psd2/v1.4.2/accounts/{accountId}/transactions";
    protected static final String CONSENTS_ENDPOINT = "/stet/psd2/v1.4.2/consents";

    public BpceGroupFetchDataService(FetchDataRestClient restClient, ProviderStateMapper providerStateMapper, FetchAccountsStrategy fetchAccountsStrategy, FetchTransactionsStrategy fetchTransactionsStrategy, FetchBalancesStrategy fetchBalancesStrategy, DateTimeSupplier dateMapper, AccountMapper accountMapper, TransactionMapper transactionMapper, DefaultProperties properties) {
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

    @Override
    protected String getConsentsEndpoint() {
        return CONSENTS_ENDPOINT;
    }

    @Override
    protected boolean shouldIgnoreProcessingAccount(StetAccountDTO account) {
        return StetAccountType.CARD.equals(account.getType());
    }
}
