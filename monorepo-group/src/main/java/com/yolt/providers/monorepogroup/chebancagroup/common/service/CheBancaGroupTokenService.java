package com.yolt.providers.monorepogroup.chebancagroup.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.CheBancaGroupToken;
import com.yolt.providers.monorepogroup.chebancagroup.common.http.CheBancaGroupHttpClient;

public interface CheBancaGroupTokenService {
    CheBancaGroupToken createClientCredentialToken(final Signer signer,
                                                   final CheBancaGroupHttpClient httpClient,
                                                   final CheBancaGroupAuthenticationMeans authenticationMeans,
                                                   final String baseRedirectUrl,
                                                   final String code) throws TokenInvalidException, JsonProcessingException;

    CheBancaGroupToken createRefreshToken(final Signer signer,
                                          final CheBancaGroupHttpClient httpClient,
                                          final CheBancaGroupAuthenticationMeans authenticationMeans,
                                          final String refreshToken) throws TokenInvalidException, JsonProcessingException;
}
