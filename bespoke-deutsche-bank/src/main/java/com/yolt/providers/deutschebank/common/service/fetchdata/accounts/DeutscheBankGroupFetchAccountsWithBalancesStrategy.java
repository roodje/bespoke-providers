package com.yolt.providers.deutschebank.common.service.fetchdata.accounts;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.Account;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.AccountsResponse;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClient;

import java.util.List;

public class DeutscheBankGroupFetchAccountsWithBalancesStrategy implements DeutscheBankGroupFetchAccountsStrategy {

    private static final String ACCOUNTS_WITH_BALANCES_ENDPOINT = "/v1/accounts?withBalance=true";

    @Override
    public List<Account> fetchAccounts(DeutscheBankGroupHttpClient httpClient,
                                       UrlFetchDataRequest request,
                                       DeutscheBankGroupProviderState providerState) throws TokenInvalidException, ProviderFetchDataException {
        try {
            AccountsResponse response = httpClient.getAccounts(ACCOUNTS_WITH_BALANCES_ENDPOINT, providerState, request.getPsuIpAddress());
            return response.getAccounts();
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
    }
}
