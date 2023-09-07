package com.yolt.providers.bancatransilvania.common.service;

import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeans;
import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupPKCE;
import com.yolt.providers.bancatransilvania.common.config.BancaTransilvaniaGroupProperties;
import com.yolt.providers.bancatransilvania.common.domain.BancaTransilvaniaGroupProviderState;
import com.yolt.providers.bancatransilvania.common.domain.model.consent.Access;
import com.yolt.providers.bancatransilvania.common.domain.model.consent.ConsentCreationRequest;
import com.yolt.providers.bancatransilvania.common.domain.model.consent.ConsentCreationResponse;
import com.yolt.providers.bancatransilvania.common.domain.model.consent.ConsentStatusResponse;
import com.yolt.providers.bancatransilvania.common.domain.model.token.TokenResponse;
import com.yolt.providers.bancatransilvania.common.http.BancaTransilvaniaGroupHttpClient;
import com.yolt.providers.bancatransilvania.common.mapper.BancaTransilvaniaGroupProviderStateMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class BancaTransilvaniaGroupAuthorizationService {

    private static final String CODE = "code";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String SCOPE = "scope";
    private static final String STATE = "state";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String GRANT_TYPE = "grant_type";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String CODE_CHALLENGE = "code_challenge";
    private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String CODE_VERIFIER = "code_verifier";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";

    private final BancaTransilvaniaGroupPKCE pkce;
    private final BancaTransilvaniaGroupProviderStateMapper providerStateMapper;
    private final BancaTransilvaniaGroupProperties properties;
    private final Clock clock;

    public RedirectStep createRedirectStepToInitiatedConsentPage(BancaTransilvaniaGroupHttpClient httpClient,
                                                                 UrlGetLoginRequest urlGetLoginRequest,
                                                                 BancaTransilvaniaGroupAuthenticationMeans authMeans) {
        ConsentCreationRequest consentCreationRequest = ConsentCreationRequest.builder()
                .access(new Access("allAccounts"))
                .recurringIndicator(true)
                .validUntil(LocalDate.now(clock).plusDays(89).toString())
                .frequencyPerDay(4)
                .combinedServiceIndicator(false)
                .build();
        try {
            ConsentCreationResponse consentCreationResponse = httpClient.postConsentCreation(
                    consentCreationRequest, urlGetLoginRequest.getPsuIpAddress());

            OAuth2ProofKeyCodeExchange codeExchange = pkce.createRandomS256();
            String consentId = consentCreationResponse.getConsentId();
            String redirectUri = urlGetLoginRequest.getBaseClientRedirectUrl();

            String redirectUrlToConsentPage = UriComponentsBuilder.fromUriString(properties.getAuthorizeUrl())
                    .queryParam(RESPONSE_TYPE, CODE)
                    .queryParam(CLIENT_ID, authMeans.getClientId())
                    .queryParam(REDIRECT_URI, redirectUri)
                    .queryParam(SCOPE, "AIS:" + consentId)
                    .queryParam(STATE, urlGetLoginRequest.getState())
                    .queryParam(CODE_CHALLENGE, codeExchange.getCodeChallenge())
                    .queryParam(CODE_CHALLENGE_METHOD, codeExchange.getCodeChallengeMethod())
                    .toUriString();

            BancaTransilvaniaGroupProviderState providerState = new BancaTransilvaniaGroupProviderState(codeExchange, consentId, redirectUri);
            return new RedirectStep(redirectUrlToConsentPage, null, providerStateMapper.toJson(providerState));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    public AccessMeansOrStepDTO createAccessMeans(BancaTransilvaniaGroupHttpClient httpClient,
                                                  BancaTransilvaniaGroupAuthenticationMeans authMeans,
                                                  UrlCreateAccessMeansRequest urlCreateAccessMeansRequest) {
        String authorizationCode = UriComponentsBuilder.fromUriString(urlCreateAccessMeansRequest.getRedirectUrlPostedBackFromSite())
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(CODE);

        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing authorization code");
        }
        try {
            BancaTransilvaniaGroupProviderState providerState = providerStateMapper.fromJson(urlCreateAccessMeansRequest.getProviderState());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(GRANT_TYPE, AUTHORIZATION_CODE);
            body.add(CODE, authorizationCode);
            body.add(REDIRECT_URI, urlCreateAccessMeansRequest.getBaseClientRedirectUrl());
            body.add(CODE_VERIFIER, providerState.getCodeVerifier());
            body.add(CLIENT_ID, authMeans.getClientId());
            body.add(CLIENT_SECRET, authMeans.getClientSecret());

            TokenResponse tokenResponse = httpClient.postAccessToken(body);
            validateTokenResponse(tokenResponse);
            providerState.setTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());

            validateConsentStatus(httpClient, providerState, urlCreateAccessMeansRequest.getPsuIpAddress());
            return new AccessMeansOrStepDTO(providerStateMapper.toAccessMeansDTO(urlCreateAccessMeansRequest.getUserId(), providerState, tokenResponse));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    private void validateConsentStatus(BancaTransilvaniaGroupHttpClient httpClient,
                                       BancaTransilvaniaGroupProviderState providerState,
                                       String psuIpAddress) throws TokenInvalidException {
        try {
            ConsentStatusResponse consentStatusResponse = httpClient.getConsentStatus(providerState, psuIpAddress);
            if (!"valid".equals(consentStatusResponse.getConsentStatus())) {
                throw new GetAccessTokenFailedException("Consent is not valid for getting data. Consent status: " + consentStatusResponse.getConsentStatus());
            }
        } catch (HttpStatusCodeException e) {
            log.warn("Something went wrong on getting consent status verification: HTTP " + e.getStatusCode());
        }
    }

    public AccessMeansDTO refreshAccessToken(BancaTransilvaniaGroupHttpClient httpClient,
                                             BancaTransilvaniaGroupAuthenticationMeans authMeans,
                                             AccessMeansDTO accessMeansDTO) throws TokenInvalidException {
        BancaTransilvaniaGroupProviderState providerState = providerStateMapper.fromJson(accessMeansDTO.getAccessMeans());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, REFRESH_TOKEN);
        body.add(REFRESH_TOKEN, providerState.getRefreshToken());
        body.add(REDIRECT_URI, providerState.getRedirectUri());
        body.add(CLIENT_ID, authMeans.getClientId());
        body.add(CLIENT_SECRET, authMeans.getClientSecret());

        TokenResponse tokenResponse = httpClient.postRefreshToken(body);
        validateTokenResponse(tokenResponse);
        providerState.setTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
        return providerStateMapper.toAccessMeansDTO(accessMeansDTO, providerState, tokenResponse);
    }

    private void validateTokenResponse(TokenResponse tokenResponse) throws TokenInvalidException {
        if (Objects.isNull(tokenResponse)) {
            throw new TokenInvalidException("Token response is null");
        }
        if (StringUtils.isEmpty(tokenResponse.getAccessToken())) {
            throw new TokenInvalidException("Access token is missing");
        }
        if (StringUtils.isEmpty(tokenResponse.getRefreshToken())) {
            throw new TokenInvalidException("Refresh token is missing");
        }
    }
}
