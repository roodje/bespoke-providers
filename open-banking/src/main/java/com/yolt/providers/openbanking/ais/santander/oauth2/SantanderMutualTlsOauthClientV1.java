package com.yolt.providers.openbanking.ais.santander.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultMutualTlsOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;

public class SantanderMutualTlsOauthClientV1 extends DefaultMutualTlsOauth2Client {

    private final TokenRequestBodyProducer tokenRequestBodyProducer;

    public SantanderMutualTlsOauthClientV1(DefaultProperties properties,
                                           TokenRequestBodyProducer tokenRequestBodyProducer,
                                           boolean isInPisFlow) {
        super(properties, any -> null, tokenRequestBodyProducer, isInPisFlow);
        this.tokenRequestBodyProducer = tokenRequestBodyProducer;
    }

    @Override
    public AccessTokenResponseDTO refreshAccessToken(final HttpClient httpClient,
                                                     final DefaultAuthMeans authenticationMeans,
                                                     final String refreshToken,
                                                     final String redirectURI,
                                                     final TokenScope scope,
                                                     final Signer signer) throws TokenInvalidException {
        Object tokenBody = tokenRequestBodyProducer.getRefreshAccessTokenBody(authenticationMeans, refreshToken, redirectURI);
        return createToken(httpClient, authenticationMeans, tokenBody, ProviderClientEndpoints.REFRESH_TOKEN, any -> null);
    }
}
