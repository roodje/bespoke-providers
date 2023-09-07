package com.yolt.providers.monorepogroup.libragroup.common.ais.auth;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans.SigningData;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupHttpClientFactory;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.*;
import com.yolt.providers.monorepogroup.libragroup.common.config.LibraGroupProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

import static com.yolt.providers.common.constants.OAuth.*;

@RequiredArgsConstructor
public class LibraGroupAuthenticationServiceV1 implements LibraGroupAuthenticationService {

    private final LibraGroupProperties properties;
    private final LibraGroupHttpClientFactory httpClientFactory;
    private final String providerIdentifier;
    private final Clock clock;

    @Override
    public LibraLoginUrlData getLoginUrlData(LibraGroupAuthenticationMeans authenticationMeans,
                                             RestTemplateManager restTemplateManager,
                                             String redirectUri,
                                             String loginState,
                                             Signer signer) {
        LibraGroupAuthenticationHttpClient httpClient =
                httpClientFactory.buildAuthorizationHttpClient(restTemplateManager);
        String accessToken = httpClient.getClientCredentialsToken(
                authenticationMeans.getClientId(),
                authenticationMeans.getClientSecret());
        LibraGroupConsent consent = httpClient.getConsent(
                authenticationMeans.getSigningData(),
                accessToken,
                signer,
                providerIdentifier);
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(SCOPE, "AIS:" + consent.getConsentId());
        requestPayload.add(RESPONSE_TYPE, CODE);
        requestPayload.add(REDIRECT_URI, URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        requestPayload.add(CLIENT_ID, authenticationMeans.getClientId());
        requestPayload.add(STATE, loginState);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(properties.getOAuthBaseUrl())
                .queryParams(requestPayload)
                .build();

        return new LibraLoginUrlData(uriComponents.toString(), consent.getConsentId());
    }

    @Override
    public LibraGroupAccessMeans getUserToken(LibraGroupAuthenticationMeans authenticationMeans,
                                              String redirectUrl,
                                              String consentId,
                                              RestTemplateManager restTemplateManager,
                                              String authCode) {
        LibraGroupAuthenticationHttpClient httpClient =
                httpClientFactory.buildAuthorizationHttpClient(restTemplateManager);
        LibraGroupAuthData authData = httpClient.getUserToken(
                authenticationMeans.getClientId(),
                authenticationMeans.getClientSecret(),
                authCode,
                redirectUrl,
                consentId,
                providerIdentifier);
        return new LibraGroupAccessMeans(new LibraGroupTokens(authData, clock), redirectUrl, consentId);
    }

    @Override
    public LibraGroupAccessMeans refreshUserToken(LibraGroupAuthenticationMeans authenticationMeans,
                                                  String refreshToken,
                                                  String redirectUrl,
                                                  String consentId,
                                                  RestTemplateManager restTemplateManager) throws TokenInvalidException {
        LibraGroupAuthenticationHttpClient httpClient = httpClientFactory.buildAuthorizationHttpClient(restTemplateManager);

        LibraGroupAuthData authData = httpClient.refreshUserToken(
                authenticationMeans.getClientId(),
                authenticationMeans.getClientSecret(),
                refreshToken,
                redirectUrl,
                providerIdentifier);
        return new LibraGroupAccessMeans(new LibraGroupTokens(authData, clock), redirectUrl, consentId);
    }

    @Override
    public void deleteConsent(SigningData signingData,
                              RestTemplateManager restTemplateManager,
                              String consentId,
                              Signer signer) throws TokenInvalidException {
        LibraGroupAuthenticationHttpClient httpClient = httpClientFactory.buildAuthorizationHttpClient(restTemplateManager);
        httpClient.deleteConsent(consentId, signingData, signer);
    }
}
