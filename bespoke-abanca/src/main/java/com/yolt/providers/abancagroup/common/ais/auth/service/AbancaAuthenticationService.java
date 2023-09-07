package com.yolt.providers.abancagroup.common.ais.auth.service;

import com.yolt.providers.abancagroup.common.AbancaHttpClient;
import com.yolt.providers.abancagroup.common.AbancaHttpClientFactory;
import com.yolt.providers.abancagroup.common.ais.auth.AbancaAuthenticationMeans;
import com.yolt.providers.abancagroup.common.ais.auth.dto.AbancaAuthData;
import com.yolt.providers.abancagroup.common.ais.auth.dto.AbancaTokens;
import com.yolt.providers.abancagroup.common.ais.config.AbancaGroupProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.util.UUID;

import static com.yolt.providers.common.constants.OAuth.*;

@RequiredArgsConstructor
public class AbancaAuthenticationService {

    private static final String SCOPE_VALUE = "Accounts Transactions";

    private static final String ACCESS_TOKEN_FAILED_MESSAGE_EMPTY_BODY = "Empty body with token response";
    private static final String ACCESS_TOKEN_FAILED_MESSAGE_ERROR = "Error during exchanging authorisation code to access token";
    private static final String AUTHORIZE_ENDPOINT_TEMPLATE = "%s/oauth/%s/Abanca";

    private final AbancaHttpClientFactory httpClientFactory;
    private final AbancaGroupProperties properties;
    private final Clock clock;

    public String getLoginUrl(UUID clientId, String redirectUri, String loginState) {
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(SCOPE, SCOPE_VALUE);
        requestPayload.add(REDIRECT_URI, redirectUri);
        requestPayload.add(STATE, loginState);
        requestPayload.add(RESPONSE_TYPE, CODE);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(String.format(AUTHORIZE_ENDPOINT_TEMPLATE, properties.getBaseUrl(), clientId))
                .queryParams(requestPayload)
                .build();

        return uriComponents.toString();
    }

    public AbancaTokens getUserToken(AbancaAuthenticationMeans authenticationMeans,
                                     RestTemplateManager restTemplateManager,
                                     String authCode) {
        AbancaHttpClient httpClient = httpClientFactory.buildHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager
        );
        try {
            AbancaAuthData authData = httpClient.getUserToken(authenticationMeans.getClientId(), authenticationMeans.getApiKey(), authCode)
                    .orElseThrow(() -> new GetAccessTokenFailedException(ACCESS_TOKEN_FAILED_MESSAGE_EMPTY_BODY));
            return new AbancaTokens(authData, clock);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(ACCESS_TOKEN_FAILED_MESSAGE_ERROR, e);
        }
    }

    public AbancaTokens refreshUserToken(String refreshToken,
                                         AbancaAuthenticationMeans authenticationMeans,
                                         final RestTemplateManager restTemplateManager) throws TokenInvalidException {
        AbancaHttpClient httpClient = httpClientFactory.buildHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager);

        AbancaAuthData authData = httpClient.refreshUserToken(authenticationMeans.getClientId(), authenticationMeans.getApiKey(), refreshToken)
                .orElseThrow(() -> new TokenInvalidException(ACCESS_TOKEN_FAILED_MESSAGE_EMPTY_BODY));
        return new AbancaTokens(authData, clock, refreshToken);
    }
}
