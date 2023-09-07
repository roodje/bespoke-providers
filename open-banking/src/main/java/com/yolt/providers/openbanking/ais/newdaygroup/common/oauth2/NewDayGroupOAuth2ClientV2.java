package com.yolt.providers.openbanking.ais.newdaygroup.common.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultClientSecretBasicOauth2Client;

import java.util.function.Function;

public class NewDayGroupOAuth2ClientV2 extends DefaultClientSecretBasicOauth2Client {

    public NewDayGroupOAuth2ClientV2(final DefaultProperties properties, final boolean isInPisFlow) {
        super(properties, isInPisFlow);
    }

    @Override
    public AccessTokenResponseDTO refreshAccessToken(HttpClient httpClient,
                                                     DefaultAuthMeans authenticationMeans,
                                                     String refreshToken,
                                                     String redirectURI,
                                                     TokenScope scope,
                                                     Signer signer) throws TokenInvalidException {
        AccessTokenResponseDTO accessTokenResponse = super.refreshAccessToken(httpClient,
                authenticationMeans,
                refreshToken,
                redirectURI,
                scope,
                signer);

        // C4PO 4604 - newday send expires_in = 600 but the real value is 300
        accessTokenResponse.setExpiresIn(300L);
        return accessTokenResponse;
    }

    @Override
    protected AccessTokenResponseDTO createToken(HttpClient httpClient,
                                                 DefaultAuthMeans authenticationMeans,
                                                 Object body,
                                                 String endpointIdentifier,
                                                 Function authenticationHeaderSupplier) throws TokenInvalidException {
        AccessTokenResponseDTO accessTokenResponseDTO = super.createToken(httpClient,
                authenticationMeans,
                body,
                endpointIdentifier,
                authenticationHeaderSupplier);
        // C4PO 4604 - newday send expires_in = 600 but the real value is 300
        accessTokenResponseDTO.setExpiresIn(300L);
        return accessTokenResponseDTO;
    }
}
