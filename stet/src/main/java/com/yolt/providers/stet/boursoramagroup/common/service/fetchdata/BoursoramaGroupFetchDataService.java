package com.yolt.providers.stet.boursoramagroup.common.service.fetchdata;

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

public class BoursoramaGroupFetchDataService extends DefaultFetchDataService {

    private static final String ACCOUNTS_ENDPOINT = "/services/api/v1.7/_user_/_%s_/dsp2/accounts";
    private static final String BALANCES_TEMPLATE = "/services/api/v1.7/_user_/_%s_/dsp2/accounts/balances/{accountId}";
    private static final String TRANSACTIONS_TEMPLATE = "/services/api/v1.7/_user_/_%s_/dsp2/accounts/transactions/{accountId}";

    public BoursoramaGroupFetchDataService(FetchDataRestClient restClient, ProviderStateMapper providerStateMapper, FetchAccountsStrategy fetchAccountsStrategy, FetchTransactionsStrategy fetchTransactionsStrategy, FetchBalancesStrategy fetchBalancesStrategy, DateTimeSupplier dateMapper, AccountMapper accountMapper, TransactionMapper transactionMapper, DefaultProperties properties) {
        super(restClient, providerStateMapper, fetchAccountsStrategy, fetchTransactionsStrategy, fetchBalancesStrategy, dateMapper, accountMapper, transactionMapper, properties);
    }

    @Override
    protected boolean shouldIgnoreProcessingAccount(StetAccountDTO account) {
        return !StetAccountType.CACC.equals(account.getType())
                && !StetAccountType.CARD.equals(account.getType());
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
    protected String getConsentsEndpoint() {
        return CONSENTS_ENDPOINT;
    }

    @Override
    protected String getAccountsEndpoint() {
        return ACCOUNTS_ENDPOINT;
    }
}