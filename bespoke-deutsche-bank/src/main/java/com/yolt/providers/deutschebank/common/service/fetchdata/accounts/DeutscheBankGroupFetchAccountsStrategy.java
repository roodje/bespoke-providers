package com.yolt.providers.deutschebank.common.service.fetchdata.accounts;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.Account;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClient;

import java.util.List;

public interface DeutscheBankGroupFetchAccountsStrategy {

    List<Account> fetchAccounts(DeutscheBankGroupHttpClient httpClient, UrlFetchDataRequest request, DeutscheBankGroupProviderState providerState) throws TokenInvalidException, ProviderFetchDataException;
}
