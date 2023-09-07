package com.yolt.providers.argentagroup.common.service.token;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import nl.ing.lovebird.providershared.AccessMeansDTO;

public interface AuthorizationService {

    AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest request,
                                              final DefaultAuthenticationMeans authenticationMeans,
                                              final HttpClient httpClient) throws TokenInvalidException;

    AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request,
                                      final DefaultAuthenticationMeans authenticationMeans,
                                      final HttpClient httpClient) throws TokenInvalidException;
}
