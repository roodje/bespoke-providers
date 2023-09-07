package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface TransactionsDataFetchService {

    List<ProviderTransactionDTO> getTransactions(final UrlFetchDataRequest request,
                                                 final DefaultAuthenticationMeans authenticationMeans,
                                                 final HttpClient httpClient,
                                                 final ProviderAccountDTO account,
                                                 final AccessMeans accessMeans) throws TokenInvalidException;
}
