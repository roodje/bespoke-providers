package com.yolt.providers.openbanking.ais.aibgroup.common.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.common.HttpUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultClientSecretBasicOauth2Client;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

import static com.yolt.providers.common.constants.OAuth.GRANT_TYPE;
import static com.yolt.providers.openbanking.ais.aibgroup.common.http.AibGroupRefreshTokenErrorHandlerV2.AIB_GROUP_REFRESH_TOKEN_ERROR_HANDLER;

public class AibGroupOAuth2ClientV2 extends DefaultClientSecretBasicOauth2Client {

    DefaultProperties properties;

    public AibGroupOAuth2ClientV2(DefaultProperties properties, boolean isInPisFlow) {
        super(properties, isInPisFlow);
        this.properties = properties;
    }

    @Override
    public AccessTokenResponseDTO refreshAccessToken(HttpClient httpClient, DefaultAuthMeans authenticationMeans,
                                                     String refreshToken, String redirectURI,
                                                     TokenScope scope, Signer signer) throws TokenInvalidException {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "refresh_token");
        body.add("refresh_token", refreshToken);
        body.add("scope", OpenBankingTokenScope.ACCOUNTS.getGrantScope());
        body.add("redirect_uri", redirectURI);

        String basicAuthenticationHeader = HttpUtils.basicCredentials(authenticationMeans.getClientId(), authenticationMeans.getClientSecret());
        HttpHeaders headers = getHeaders(basicAuthenticationHeader, authenticationMeans.getInstitutionId());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return httpClient.exchange(properties.getOAuthTokenUrl(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                ProviderClientEndpoints.REFRESH_TOKEN,
                AccessTokenResponseDTO.class,
                AIB_GROUP_REFRESH_TOKEN_ERROR_HANDLER).getBody();
    }
}
