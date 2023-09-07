package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;

import java.time.Instant;

public interface RaiffeisenAtGroupFetchDataService {

    FetchDataResult fetchData(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final String providerState, final String psuIpAddress, final Instant transactionsFetchStartTime) throws TokenInvalidException, ProviderFetchDataException;
}
