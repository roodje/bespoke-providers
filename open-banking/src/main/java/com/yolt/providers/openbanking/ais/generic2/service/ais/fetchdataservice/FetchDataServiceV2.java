package com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;

import java.time.Instant;

public interface FetchDataServiceV2 {

    DataProviderResponse getAccountsAndTransactions(final HttpClient httpClient,
                                                    final DefaultAuthMeans authenticationMeans,
                                                    final Instant transactionsFetchStartTime,
                                                    final AccessMeansState accessMeansState) throws TokenInvalidException, ProviderFetchDataException;
}
