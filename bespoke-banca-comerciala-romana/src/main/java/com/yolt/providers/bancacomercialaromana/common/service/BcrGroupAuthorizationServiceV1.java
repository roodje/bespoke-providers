package com.yolt.providers.bancacomercialaromana.common.service;

import com.yolt.providers.bancacomercialaromana.common.auth.BcrGroupAuthenticationMeans;
import com.yolt.providers.bancacomercialaromana.common.configuration.BcrGroupProperties;
import com.yolt.providers.bancacomercialaromana.common.http.BcrGroupHttpClient;
import com.yolt.providers.bancacomercialaromana.common.model.Token;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import static com.yolt.providers.bancacomercialaromana.common.model.enumeration.GrantType.AUTHORIZATION_CODE;
import static com.yolt.providers.bancacomercialaromana.common.model.enumeration.GrantType.REFRESH_TOKEN;

@RequiredArgsConstructor
public class BcrGroupAuthorizationServiceV1 {

    public static final String CODE_NAME = "code";
    private static final String SCOPE_VALUE = "AISP PISP";
    private static final String RESPONSE_TYPE_NAME = "response_type";
    private static final String CLIENT_ID_NAME = "client_id";
    private static final String REDIRECT_URI_NAME = "redirect_uri";
    private static final String SCOPE_NAME = "scope";
    private static final String STATE_NAME = "state";
    private static final String GRANT_TYPE_NAME = "grant_type";
    private static final String ACCESS_TYPE_NAME = "access_type";
    private static final String ACCESS_TYPE = "offline";

    private final BcrGroupProperties properties;

    public String getLoginUrl(String clientId, String redirectUrl, String state) {
        MultiValueMap<String, String> httpQueryParams = new LinkedMultiValueMap<>();
        httpQueryParams.add(RESPONSE_TYPE_NAME, CODE_NAME);
        httpQueryParams.add(CLIENT_ID_NAME, clientId);
        httpQueryParams.add(REDIRECT_URI_NAME, redirectUrl);
        httpQueryParams.add(SCOPE_NAME, SCOPE_VALUE);
        httpQueryParams.add(STATE_NAME, state);
        httpQueryParams.add(ACCESS_TYPE_NAME, ACCESS_TYPE);

        return UriComponentsBuilder
                .fromHttpUrl(properties.getAuthorizationBaseUrl())
                .queryParams(httpQueryParams)
                .toUriString();
    }

    public Token getAccessTokenUsingAuthorizationCode(BcrGroupHttpClient httpClient,
                                                      String redirectUrl,
                                                      String authorizationCode,
                                                      BcrGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException {
        String clientId = authenticationMeans.getClientId();
        String clientSecret = authenticationMeans.getClientSecret();

        MultiValueMap<String, String> httpQueryParams = new LinkedMultiValueMap<>();
        httpQueryParams.add(GRANT_TYPE_NAME, AUTHORIZATION_CODE.getValue());
        httpQueryParams.add(CODE_NAME, authorizationCode);
        httpQueryParams.add(REDIRECT_URI_NAME, redirectUrl);

        return httpClient.postForToken(clientId, clientSecret, ProviderClientEndpoints.GET_ACCESS_TOKEN, httpQueryParams);
    }

    public Token getAccessTokenUsingRefreshToken(BcrGroupHttpClient httpClient,
                                                 String refreshToken,
                                                 BcrGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException {
        String clientId = authenticationMeans.getClientId();
        String clientSecret = authenticationMeans.getClientSecret();

        MultiValueMap<String, String> httpQueryParams = new LinkedMultiValueMap<>();
        httpQueryParams.add(GRANT_TYPE_NAME, REFRESH_TOKEN.getValue());
        httpQueryParams.add(REFRESH_TOKEN.getValue(), refreshToken);

        return httpClient.postForToken(clientId, clientSecret, ProviderClientEndpoints.REFRESH_TOKEN, httpQueryParams);
    }
}
