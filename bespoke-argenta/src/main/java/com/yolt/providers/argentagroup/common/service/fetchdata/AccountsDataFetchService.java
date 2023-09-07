package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.util.List;

public interface AccountsDataFetchService {

    List<ProviderAccountDTO> getAccounts(final UrlFetchDataRequest request,
                                         final DefaultAuthenticationMeans authenticationMeans,
                                         final HttpClient httpClient,
                                         final AccessMeans accessMeans) throws TokenInvalidException;
}
