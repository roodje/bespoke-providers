package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.service;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.bankvanbredagroup.BankVanBredaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.*;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.service.BankVanBredaGroupHttpClientFactory;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

import static com.yolt.providers.common.constants.OAuth.*;

@RequiredArgsConstructor
public class BankVanBredaGroupAuthenticationService {

    private static final String SCOPE_VALUE_TEMPLATE = "AIS:%s";

    protected static final String CODE_CHALLENGE = "code_challenge";
    protected static final String CODE_CHALLENGE_METHOD = "code_challenge_method";

    private final BankVanBredaGroupHttpClientFactory httpClientFactory;
    private final String providerIdentifier;
    private final Clock clock;

    public BankVanBredaLoginUrlData getLoginUrlData(BankVanBredaGroupAuthenticationMeans authenticationMeans,
                                                    RestTemplateManager restTemplateManager,
                                                    String redirectUri,
                                                    String loginState,
                                                    String psuIp) {
        BankVanBredaGroupAuthenticationHttpClient httpClient = httpClientFactory.buildAuthorizationHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager
        );
        BankVanBredaGroupConsent consent = httpClient.getConsent(redirectUri, psuIp, providerIdentifier);
        String authorizationEndpoint = httpClient.getBankLoginUrl(consent.scaOAuth(), providerIdentifier);
        OAuth2ProofKeyCodeExchange proofKeyCodeExchange = OAuth2ProofKeyCodeExchange.createRandomS256();
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        String scope = String.format(SCOPE_VALUE_TEMPLATE, consent.getConsentId());
        requestPayload.add(SCOPE, scope);
        requestPayload.add(CLIENT_ID, authenticationMeans.getTppId());
        requestPayload.add(STATE, loginState);
        requestPayload.add(REDIRECT_URI, URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        requestPayload.add(CODE_CHALLENGE, proofKeyCodeExchange.getCodeChallenge());
        requestPayload.add(CODE_CHALLENGE_METHOD, proofKeyCodeExchange.getCodeChallengeMethod());
        requestPayload.add(RESPONSE_TYPE, CODE);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(authorizationEndpoint)
                .queryParams(requestPayload)
                .build();

        return new BankVanBredaLoginUrlData(uriComponents.toString(), proofKeyCodeExchange.getCodeVerifier(), consent.getConsentId());
    }

    public BankVanBredaGroupAccessMeans getUserToken(BankVanBredaGroupAuthenticationMeans authenticationMeans,
                                                     String redirectUrl,
                                                     String codeVerifier,
                                                     RestTemplateManager restTemplateManager,
                                                     String authCode) {
        BankVanBredaGroupAuthenticationHttpClient httpClient = httpClientFactory.buildAuthorizationHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager
        );
        BankVanBredaGroupAuthData authData = httpClient.getUserToken(
                authCode,
                authenticationMeans.getTppId(),
                codeVerifier,
                redirectUrl,
                providerIdentifier);
        return new BankVanBredaGroupAccessMeans(new BankVanBredaGroupTokens(authData, clock), retrieveConsentId(authData.getScope()));
    }

    public BankVanBredaGroupAccessMeans refreshUserToken(String refreshToken,
                                                         BankVanBredaGroupAuthenticationMeans authenticationMeans,
                                                         RestTemplateManager restTemplateManager) throws TokenInvalidException {
        BankVanBredaGroupAuthenticationHttpClient httpClient = httpClientFactory.buildAuthorizationHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager);

        BankVanBredaGroupAuthData authData = httpClient.refreshUserToken(
                refreshToken,
                authenticationMeans.getTppId(),
                providerIdentifier);
        return new BankVanBredaGroupAccessMeans(new BankVanBredaGroupTokens(authData, clock, refreshToken), retrieveConsentId(authData.getScope()));
    }

    public void deleteConsent(BankVanBredaGroupAuthenticationMeans authenticationMeans,
                              RestTemplateManager restTemplateManager,
                              String consentId) throws TokenInvalidException {
        BankVanBredaGroupAuthenticationHttpClient httpClient = httpClientFactory.buildAuthorizationHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager);
        httpClient.deleteConsent(consentId);
    }

    private String retrieveConsentId(String scope) {
        String consentId = StringUtils.removeStartIgnoreCase(scope, "AIS:");
        if (StringUtils.isEmpty(consentId)) {
            throw new GetAccessTokenFailedException("Missing consentId in token data");
        }
        return consentId;
    }

}
