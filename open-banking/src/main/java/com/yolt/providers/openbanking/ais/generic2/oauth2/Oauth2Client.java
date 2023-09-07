package com.yolt.providers.openbanking.ais.generic2.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;

public interface Oauth2Client {

    AccessTokenResponseDTO refreshAccessToken(final HttpClient httpClient,
                                              final DefaultAuthMeans authenticationMeans,
                                              final String refreshToken,
                                              final String redirectURI,
                                              final TokenScope scope,
                                              final Signer signer) throws TokenInvalidException;

    AccessTokenResponseDTO createAccessToken(final HttpClient httpClient,
                                             final DefaultAuthMeans authenticationMeans,
                                             final String authorizationCode,
                                             final String redirectURI,
                                             final TokenScope scope,
                                             final Signer signer) throws TokenInvalidException;

    AccessTokenResponseDTO createClientCredentials(final HttpClient httpClient,
                                                   final DefaultAuthMeans authenticationMeans,
                                                   final TokenScope scope,
                                                   final Signer signer) throws TokenInvalidException;
}
