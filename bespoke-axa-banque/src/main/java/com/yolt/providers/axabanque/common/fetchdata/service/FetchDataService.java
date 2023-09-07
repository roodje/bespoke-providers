package com.yolt.providers.axabanque.common.fetchdata.service;

import com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;

import java.time.Instant;

public interface FetchDataService {
    DataProviderResponse fetchData(final GroupAccessMeans accessMeans,
                                   final GroupAuthenticationMeans authenticationMeans,
                                   final RestTemplateManager restTemplateManager,
                                   final Instant transactionFetchStartTime,
                                   final String psuIpAddress) throws TokenInvalidException, ProviderFetchDataException;
}
