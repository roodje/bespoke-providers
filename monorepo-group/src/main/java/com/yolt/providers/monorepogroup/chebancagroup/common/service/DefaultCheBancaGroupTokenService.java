package com.yolt.providers.monorepogroup.chebancagroup.common.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.CheBancaGroupToken;
import com.yolt.providers.monorepogroup.chebancagroup.common.http.CheBancaGroupHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;

@RequiredArgsConstructor
public class DefaultCheBancaGroupTokenService implements CheBancaGroupTokenService {

    @Override
    public CheBancaGroupToken createClientCredentialToken(final Signer signer,
                                                          final CheBancaGroupHttpClient httpClient,
                                                          final CheBancaGroupAuthenticationMeans authenticationMeans,
                                                          final String baseRedirectUri,
                                                          final String code) throws TokenInvalidException {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(CLIENT_ID, authenticationMeans.getClientId());
        requestBody.add(CLIENT_SECRET, authenticationMeans.getClientSecret());
        requestBody.add(GRANT_TYPE, AUTHORIZATION_CODE);
        requestBody.add(REDIRECT_URI, baseRedirectUri);
        requestBody.add(CODE, code);
        return httpClient.createClientCredentialToken(signer, requestBody, authenticationMeans);
    }

    @Override
    public CheBancaGroupToken createRefreshToken(final Signer signer,
                                                 final CheBancaGroupHttpClient httpClient,
                                                 final CheBancaGroupAuthenticationMeans authenticationMeans,
                                                 final String refreshToken) throws TokenInvalidException {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(CLIENT_ID, authenticationMeans.getClientId());
        requestBody.add(CLIENT_SECRET, authenticationMeans.getClientSecret());
        requestBody.add(GRANT_TYPE, REFRESH_TOKEN);
        requestBody.add(REFRESH_TOKEN, refreshToken);
        return httpClient.createRefreshToken(signer, requestBody, authenticationMeans);
    }
}
