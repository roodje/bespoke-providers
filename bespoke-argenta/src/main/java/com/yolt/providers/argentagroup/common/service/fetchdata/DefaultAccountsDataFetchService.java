package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.argentagroup.dto.GetAccountsResponse;
import com.yolt.providers.argentagroup.dto.GetAccountsResponseAccounts;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class DefaultAccountsDataFetchService implements AccountsDataFetchService {

    private final String accountsEndpointPath;
    private final FetchDataHttpHeadersProvider fetchDataHttpHeadersProvider;
    private final HttpErrorHandler accountsHttpErrorHandler;
    private final AccountsMapper accountsMapper;

    @Override
    public List<ProviderAccountDTO> getAccounts(final UrlFetchDataRequest request,
                                                final DefaultAuthenticationMeans authenticationMeans,
                                                final HttpClient httpClient,
                                                final AccessMeans accessMeans) throws TokenInvalidException {
        HttpHeaders requestHeaders = fetchDataHttpHeadersProvider.providerHeaders(request, authenticationMeans, accessMeans);

        GetAccountsResponse accountsResponse = httpClient.exchange(
                accountsEndpointPath,
                HttpMethod.GET,
                new HttpEntity<>(requestHeaders),
                ProviderClientEndpoints.GET_ACCOUNTS,
                GetAccountsResponse.class,
                accountsHttpErrorHandler
        ).getBody();

        List<ProviderAccountDTO> accounts = new ArrayList<>();
        for (GetAccountsResponseAccounts accountResponse : accountsResponse.getAccounts()) {
            ProviderAccountDTO account = accountsMapper.map(accountResponse);
            accounts.add(account);
        }

        return accounts;
    }
}
