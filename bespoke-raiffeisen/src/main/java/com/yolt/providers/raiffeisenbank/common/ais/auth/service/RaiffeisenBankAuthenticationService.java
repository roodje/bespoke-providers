package com.yolt.providers.raiffeisenbank.common.ais.auth.service;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.raiffeisenbank.common.RaiffeisenBankHttpClient;
import com.yolt.providers.raiffeisenbank.common.RaiffeisenBankHttpClientFactory;
import com.yolt.providers.raiffeisenbank.common.ais.auth.RaiffeisenBankAuthenticationMeans;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.RaiffeisenAuthData;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.RaiffeisenBankTokens;
import com.yolt.providers.raiffeisenbank.common.ais.config.RaiffeisenBankProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.yolt.providers.common.constants.OAuth.*;
import static net.logstash.logback.marker.Markers.append;

@RequiredArgsConstructor
public class RaiffeisenBankAuthenticationService {

    private static final String SCOPE_VALUE = "AISP";

    private static final String ACCESS_TOKEN_FAILED_MESSAGE_EMPTY_BODY = "Empty body with token response";
    private static final String ACCESS_TOKEN_FAILED_MESSAGE_ERROR = "Error during exchanging authorisation code to access token";
    private static final String AUTHORIZE_ENDPOINT = "/psd2-rbro-oauth2-api/aisp/oauth2/authorize";
    private static final String CONSENT_ID = "consentId";

    private final RaiffeisenBankHttpClientFactory httpClientFactory;
    private final RaiffeisenBankProperties properties;
    private final Clock clock;


    public String getLoginUrl(String clientId, String consentId, String redirectUri, String loginState) {
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(CLIENT_ID, clientId);
        requestPayload.add(SCOPE, SCOPE_VALUE);
        requestPayload.add(REDIRECT_URI, redirectUri);
        requestPayload.add(STATE, loginState);
        requestPayload.add(RESPONSE_TYPE, CODE);
        requestPayload.add(CONSENT_ID, consentId);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(properties.getOAuthBaseUrl() + AUTHORIZE_ENDPOINT)
                .queryParams(requestPayload)
                .build();

        return uriComponents.toString();
    }

    public Optional<String> createConsentId(RaiffeisenBankAuthenticationMeans authenticationMeans,
                                            String redirectUrl,
                                            String psuIp,
                                            String iban,
                                            String accountLogin,
                                            RestTemplateManager restTemplateManager) throws TokenInvalidException {
        RaiffeisenBankHttpClient httpClient = httpClientFactory.buildHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager);

        return httpClient.createConsentId(
                authenticationMeans.getClientId(),
                redirectUrl,
                psuIp,
                iban,
                accountLogin);
    }

    public RaiffeisenBankTokens getUserToken(RaiffeisenBankAuthenticationMeans authenticationMeans,
                                             RestTemplateManager restTemplateManager,
                                             String authCode,
                                             String redirectUri) {
        RaiffeisenBankHttpClient httpClient = httpClientFactory.buildHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager);
        try {
            RaiffeisenAuthData authData = httpClient.getUserToken(authenticationMeans.getClientId(), authenticationMeans.getClientSecret(), authCode, redirectUri)
                    .orElseThrow(() -> new GetAccessTokenFailedException(ACCESS_TOKEN_FAILED_MESSAGE_EMPTY_BODY));
            return new RaiffeisenBankTokens(authData, clock);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(ACCESS_TOKEN_FAILED_MESSAGE_ERROR, e);
        }
    }

    public RaiffeisenBankTokens refreshUserToken(String refreshToken,
                                                 RaiffeisenBankAuthenticationMeans authenticationMeans,
                                                 final RestTemplateManager restTemplateManager) throws TokenInvalidException {
        RaiffeisenBankHttpClient httpClient = httpClientFactory.buildHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager);

        RaiffeisenAuthData authData = httpClient.refreshUserToken(authenticationMeans.getClientId(), authenticationMeans.getClientSecret(), refreshToken)
                .orElseThrow(() -> new TokenInvalidException(ACCESS_TOKEN_FAILED_MESSAGE_EMPTY_BODY));
        return new RaiffeisenBankTokens(authData, clock);
    }

    public void deleteConsent(String consentId,
                              String accessToken,
                              RaiffeisenBankAuthenticationMeans authenticationMeans,
                              RestTemplateManager restTemplateManager) throws TokenInvalidException {
        RaiffeisenBankHttpClient httpClient = httpClientFactory.buildHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager);
        httpClient.deleteConsent(consentId, accessToken, authenticationMeans.getClientId());
    }

}
