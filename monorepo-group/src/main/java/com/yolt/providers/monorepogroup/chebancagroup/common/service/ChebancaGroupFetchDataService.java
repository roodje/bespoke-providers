package com.yolt.providers.monorepogroup.chebancagroup.common.service;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.chebancagroup.common.http.CheBancaGroupHttpClient;

import java.time.Instant;

public interface ChebancaGroupFetchDataService {

    FetchDataResult fetchData(final UrlFetchDataRequest fetchDataRequest, final Signer signer, final CheBancaGroupHttpClient httpClient, final CheBancaGroupAuthenticationMeans authenticationMeans, final Instant transactionsFetchStartTime) throws TokenInvalidException, ProviderFetchDataException;
}
