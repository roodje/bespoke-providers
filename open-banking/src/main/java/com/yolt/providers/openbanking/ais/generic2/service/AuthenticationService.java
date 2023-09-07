package com.yolt.providers.openbanking.ais.generic2.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

import java.util.UUID;

public interface AuthenticationService {

    String generateAuthorizationUrl(final DefaultAuthMeans authenticationMeans,
                                    final String resourceId,
                                    final String secretState,
                                    final String redirectUrl,
                                    final TokenScope scope,
                                    final Signer signer);

    AccessMeans createAccessToken(final HttpClient httpClient,
                                  final DefaultAuthMeans authenticationMeans,
                                  final UUID userId,
                                  final String authorizationCode,
                                  final String redirectUrl,
                                  final TokenScope scope,
                                  final Signer signer) throws TokenInvalidException;

    AccessMeans getClientAccessToken(final HttpClient httpClient,
                                     final DefaultAuthMeans authenticationMeans,
                                     final AuthenticationMeansReference authenticationMeansReference,
                                     final TokenScope scope,
                                     final Signer signer);

    AccessMeans refreshAccessToken(final HttpClient httpClient,
                                   final DefaultAuthMeans authenticationMeans,
                                   final UUID userId,
                                   final String refreshToken,
                                   final String redirectUrl,
                                   final TokenScope scope,
                                   final Signer signer) throws TokenInvalidException;

    AccessMeans getClientAccessTokenWithoutCache(final HttpClient httpClient,
                                                 final DefaultAuthMeans authenticationMeans,
                                                 final TokenScope scope,
                                                 final Signer signer);
}
