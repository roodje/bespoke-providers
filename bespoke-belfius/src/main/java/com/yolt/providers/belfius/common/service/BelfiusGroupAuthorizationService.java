package com.yolt.providers.belfius.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.belfius.common.exception.UnexpectedJsonElementException;
import com.yolt.providers.belfius.common.http.client.BelfiusGroupHttpClient;
import com.yolt.providers.belfius.common.http.client.BelfiusGroupTokenHttpClient;
import com.yolt.providers.belfius.common.model.BelfiusGroupAccessMeans;
import com.yolt.providers.belfius.common.model.BelfiusGroupAccessToken;
import com.yolt.providers.belfius.common.model.BelfiusGroupProviderState;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@RequiredArgsConstructor
public abstract class BelfiusGroupAuthorizationService {

    private final ObjectMapper mapper;

    public RedirectStep getLoginUrlForUser(BelfiusGroupHttpClient belfiusGroupHttpClient,
                                           UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        OAuth2ProofKeyCodeExchange oAuth2ProofKeyCodeExchange = createRandomS256();

        BelfiusGroupProviderState belfiusGroupProviderState = new BelfiusGroupProviderState(
                urlCreateAccessMeans.getFilledInUserSiteFormValues().get("ConsentLanguage"),
                oAuth2ProofKeyCodeExchange.getCodeVerifier()
        );

        String belfiusGroupProviderStateAsString = getBelfiusGroupProviderStateAsString(belfiusGroupProviderState);

        String loginUrl = belfiusGroupHttpClient.getLoginUrl(urlCreateAccessMeans, oAuth2ProofKeyCodeExchange);

        loginUrl += "&state=" + urlCreateAccessMeans.getState();

        return new RedirectStep(loginUrl, null, belfiusGroupProviderStateAsString);
    }

    /*Yolt security library has an issue
    OAuth2ProofKeyCodeExchange.createRandomS256() is String codeVerifier = Base64.getEncoder().encodeToString(verifier)
    but should be String codeVerifier = Base64.getUrlEncoder().encodeToString(verifier);
    TODO OAuth2ProofKeyCodeExchange.createRandomS256() should be corrected in security util library */
    private OAuth2ProofKeyCodeExchange createRandomS256() {
        byte[] verifier = new byte[33];
        (new SecureRandom()).nextBytes(verifier);

        try {
            String codeVerifier = Base64.getUrlEncoder().encodeToString(verifier).replace("=", "");
            byte[] challenge = MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes());
            String codeChallenge = Base64.getUrlEncoder().encodeToString(challenge).replace("=", "");
            String codeChallengeMethod = "S256";
            return new OAuth2ProofKeyCodeExchange(codeVerifier, codeChallenge, codeChallengeMethod);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to generate a SHA-256 challenge for PKCE.", e);
        }
    }

    private String getBelfiusGroupProviderStateAsString(BelfiusGroupProviderState belfiusGroupProviderState) {
        try {
            return mapper.writeValueAsString(belfiusGroupProviderState);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize BelfiusGroupProviderState");
        }
    }

    public BelfiusGroupAccessMeans getAccessToken(BelfiusGroupTokenHttpClient belfiusGroupHttpClient,
                                                  UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        String redirectUrlWithoutQueryParamsFromAuthMeans = urlCreateAccessMeans.getBaseClientRedirectUrl();

        String authorizationCodeFromAccessMeans = UriComponentsBuilder
                .fromUriString(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite())
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get("code");

        if (StringUtils.isEmpty(authorizationCodeFromAccessMeans)) {
            throw new MissingDataException("Missing data for key authorization_code.");
        }

        BelfiusGroupProviderState providerState = deserializeProviderState(urlCreateAccessMeans.getProviderState());

        BelfiusGroupAccessToken accessToken = belfiusGroupHttpClient.getAccessToken(
                redirectUrlWithoutQueryParamsFromAuthMeans,
                authorizationCodeFromAccessMeans,
                providerState.getCodeVerifier()
        );

        return new BelfiusGroupAccessMeans(accessToken, providerState.getLanguage(), urlCreateAccessMeans.getBaseClientRedirectUrl());
    }

    private BelfiusGroupProviderState deserializeProviderState(String providerState) {
        try {
            return mapper.readValue(providerState, BelfiusGroupProviderState.class);
        } catch (IOException e) {
            throw new UnexpectedJsonElementException("Unable to obtain token from access means");
        }
    }

    public BelfiusGroupAccessMeans getAccessTokenUsingRefreshToken(BelfiusGroupTokenHttpClient belfiusGroupHttpClient,
                                                                   BelfiusGroupAccessMeans accessMeans) throws TokenInvalidException {
        BelfiusGroupAccessToken accessTokenUsingRefreshToken = belfiusGroupHttpClient.getAccessTokenUsingRefreshToken(
                accessMeans.getAccessToken().getRefreshToken());

        return new BelfiusGroupAccessMeans(accessTokenUsingRefreshToken, accessMeans.getLanguage(), accessMeans.getRedirectUrl());
    }
}
