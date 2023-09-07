package com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;

import java.time.Instant;

@Deprecated //Use newer version with AccessMeansState C4PO-8398
public interface FetchDataService {

    DataProviderResponse getAccountsAndTransactions(final HttpClient httpClient,
                                                    final DefaultAuthMeans authenticationMeans,
                                                    final Instant transactionsFetchStartTime,
                                                    final AccessMeans accessToken) throws TokenInvalidException, ProviderFetchDataException;
}
