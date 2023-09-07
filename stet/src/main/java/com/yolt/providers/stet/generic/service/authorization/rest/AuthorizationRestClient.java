package com.yolt.providers.stet.generic.service.authorization.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;

public interface AuthorizationRestClient {

    <T, U extends AccessTokenRequest> T getAccessToken(HttpClient httpClient,
                                                       U accessTokenRequestDTO,
                                                       DefaultAuthenticationMeans authMeans,
                                                       Class<T> accessTokenClass) throws TokenInvalidException;

    <T, U extends RefreshTokenRequest> T refreshAccessToken(HttpClient httpClient,
                                                            U refreshTokenRequestDTO,
                                                            DefaultAuthenticationMeans authMeans,
                                                            Class<T> refreshTokenClass) throws TokenInvalidException;
}
