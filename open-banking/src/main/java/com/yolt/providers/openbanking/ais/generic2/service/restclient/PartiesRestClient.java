package com.yolt.providers.openbanking.ais.generic2.service.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;

public interface PartiesRestClient {
    <T> T callForParties(final HttpClient httpClient,
                              final String exchangePath,
                              final AccessMeans clientAccessToken,
                              final DefaultAuthMeans authMeans,
                              final Class<T> responseType) throws TokenInvalidException;
}
