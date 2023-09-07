package com.yolt.providers.stet.bnpparibasgroup.common.service.fetchdata;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountUsage;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.AccountMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.mapper.transaction.TransactionMapper;
import com.yolt.providers.stet.generic.service.fetchdata.DefaultFetchDataService;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.FetchTransactionsStrategy;

public class BnpParibasGroupFetchDataService extends DefaultFetchDataService {
    public BnpParibasGroupFetchDataService(FetchDataRestClient restClient, ProviderStateMapper providerStateMapper, FetchAccountsStrategy fetchAccountsStrategy, FetchTransactionsStrategy fetchTransactionsStrategy, FetchBalancesStrategy fetchBalancesStrategy, DateTimeSupplier dateMapper, AccountMapper accountMapper, TransactionMapper transactionMapper, DefaultProperties properties) {
        super(restClient, providerStateMapper, fetchAccountsStrategy, fetchTransactionsStrategy, fetchBalancesStrategy, dateMapper, accountMapper, transactionMapper, properties);
    }

    @Override
    protected boolean shouldIgnoreProcessingAccount(StetAccountDTO account) {
        return StetAccountUsage.ORGA.equals(account.getUsage());
    }
}
