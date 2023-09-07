package com.yolt.providers.openbanking.ais.cybgroup.common.auth;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.common.HttpUtils;
import com.yolt.providers.openbanking.ais.cybgroup.common.config.CybgGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.http.CybgGroupRefreshTokenErrorHandlerV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.producer.CybgGroupTokenBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.util.function.Function;

public class CybgGroupClientSecretBasicOauth2Client extends BasicOauthClient<MultiValueMap<String, String>> {

    private final String oAuthTokenUrl;
    private final CybgGroupRefreshTokenErrorHandlerV2 refreshTokenErrorHandler;

    public CybgGroupClientSecretBasicOauth2Client(CybgGroupTokenBodyProducer tokenBodyProducer, CybgGroupPropertiesV2 properties, boolean isInPisFlow) {
        super(properties.getOAuthTokenUrl(),
                authenticationMeans -> HttpUtils.basicCredentials(authenticationMeans.getClientId(), authenticationMeans.getClientSecret()),
                tokenBodyProducer,
                isInPisFlow);
        this.oAuthTokenUrl = properties.getOAuthTokenUrl();
        this.refreshTokenErrorHandler = new CybgGroupRefreshTokenErrorHandlerV2();
    }

    @Override
    protected AccessTokenResponseDTO createToken(final HttpClient httpClient,
                                                 final DefaultAuthMeans authenticationMeans,
                                                 final MultiValueMap<String, String> body,
                                                 final String endpointIdentifier,
                                                 Function<DefaultAuthMeans, String> authenticationHeaderSupplier) throws TokenInvalidException {
        HttpHeaders headers = getHeaders(authenticationHeaderSupplier.apply(authenticationMeans), authenticationMeans.getInstitutionId());
        return httpClient.exchange(oAuthTokenUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                endpointIdentifier,
                AccessTokenResponseDTO.class,
                refreshTokenErrorHandler)
                .getBody();
    }
}