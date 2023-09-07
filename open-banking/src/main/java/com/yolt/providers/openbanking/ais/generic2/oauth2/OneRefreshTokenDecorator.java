package com.yolt.providers.openbanking.ais.generic2.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

@AllArgsConstructor
public class OneRefreshTokenDecorator implements Oauth2Client {

    private final Oauth2Client wrappee;

    @Override
    public AccessTokenResponseDTO refreshAccessToken(HttpClient httpClient, DefaultAuthMeans authenticationMeans, String refreshToken, String redirectURI, TokenScope scope, Signer signer) throws TokenInvalidException {
        AccessTokenResponseDTO accessTokenResponseDTO = wrappee.refreshAccessToken(httpClient, authenticationMeans, refreshToken, redirectURI, scope, signer);
        //TODO C4PO-11529 when all banks released their changes this if should be deleted
        //we should exactly know bank's behavior
        if (ObjectUtils.isEmpty(accessTokenResponseDTO.getRefreshToken())) {
            accessTokenResponseDTO.setRefreshToken(refreshToken);
        }
        return accessTokenResponseDTO;
    }

    @Override
    public AccessTokenResponseDTO createAccessToken(HttpClient httpClient, DefaultAuthMeans authenticationMeans, String authorizationCode, String redirectURI, TokenScope scope, Signer signer) throws TokenInvalidException {
        return wrappee.createAccessToken(httpClient, authenticationMeans, authorizationCode, redirectURI, scope, signer);
    }

    @Override
    public AccessTokenResponseDTO createClientCredentials(HttpClient httpClient, DefaultAuthMeans authenticationMeans, TokenScope scope, Signer signer) throws TokenInvalidException {
        return wrappee.createClientCredentials(httpClient, authenticationMeans, scope, signer);
    }
}
