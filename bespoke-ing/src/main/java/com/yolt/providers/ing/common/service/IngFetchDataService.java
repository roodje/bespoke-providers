package com.yolt.providers.ing.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngUserAccessMeans;

import java.time.Clock;
import java.time.Instant;

public interface IngFetchDataService {
    DataProviderResponse fetchData(final IngUserAccessMeans accessMeans,
                                   final IngAuthenticationMeans authenticationMeans,
                                   final RestTemplateManager restTemplateManager,
                                   final Signer signer,
                                   final Instant transactionFetchStartTime,
                                   final Clock clock) throws ProviderFetchDataException, TokenInvalidException;
}
