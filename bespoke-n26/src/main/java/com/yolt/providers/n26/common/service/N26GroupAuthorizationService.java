package com.yolt.providers.n26.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.n26.common.auth.N26GroupPKCE;
import com.yolt.providers.n26.common.config.BaseN26Properties;
import com.yolt.providers.n26.common.dto.N26GroupProviderState;
import com.yolt.providers.n26.common.dto.ais.consent.Access;
import com.yolt.providers.n26.common.dto.ais.consent.ConsentCreationRequest;
import com.yolt.providers.n26.common.dto.ais.consent.ConsentCreationResponse;
import com.yolt.providers.n26.common.dto.token.TokenResponse;
import com.yolt.providers.n26.common.http.N26GroupHttpClient;
import com.yolt.providers.n26.common.service.mapper.N26GroupProviderStateMapper;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import static com.yolt.providers.n26.common.auth.N26GroupAuthenticationMeansProducerV1.CLIENT_ID_NAME;

@RequiredArgsConstructor
public class N26GroupAuthorizationService {

    private static final String CODE = "code";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String SCOPE = "scope";
    private static final String STATE = "state";
    private static final String CODE_VERIFIER = "code_verifier";
    private static final String REQUEST_ID = "request_id";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String GRANT_TYPE = "grant_type";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String CODE_CHALLENGE = "code_challenge";
    private static final String CLIENT_ID = "client_id";
    private static final String ALL_ACCOUNTS = "allAccounts";
    private static final String DEDICATED_AISP = "DEDICATED_AISP";

    private final N26GroupProviderStateMapper providerStateMapper;
    private final N26GroupPKCE pkce;
    private final BaseN26Properties properties;
    private final ConsentStatusPoller consentStatusPoller;
    private final Clock clock;

    public RedirectStep createRedirectStepToConsentPage(N26GroupHttpClient httpClient,
                                                        UrlGetLoginRequest urlGetLoginRequest) {
        try {
            OAuth2ProofKeyCodeExchange codeExchange = pkce.createRandomS256();
            String redirectUri = urlGetLoginRequest.getBaseClientRedirectUrl();

            String authorizeUrl = UriComponentsBuilder.fromUriString(properties.getAuthorizationUrl())
                    .queryParam(RESPONSE_TYPE, CODE)
                    .queryParam(REDIRECT_URI, redirectUri)
                    .queryParam(CLIENT_ID, urlGetLoginRequest.getAuthenticationMeans().get(CLIENT_ID_NAME).getValue())
                    .queryParam(SCOPE, DEDICATED_AISP)
                    .queryParam(CODE_CHALLENGE, codeExchange.getCodeChallenge())
                    .queryParam(STATE, urlGetLoginRequest.getState())
                    .toUriString();

            String authorizeResponse = Objects.requireNonNull(httpClient.getAuthorize(authorizeUrl).getHeaders().getLocation()).toString();
            String requestId = UriComponentsBuilder
                    .fromUriString(authorizeResponse)
                    .build()
                    .getQueryParams()
                    .getFirst("requestId");

            N26GroupProviderState providerState = new N26GroupProviderState(codeExchange, redirectUri, requestId, clock);
            UUID dummyExternalConsentId = UUID.randomUUID();
            return new RedirectStep(authorizeResponse, dummyExternalConsentId.toString(), providerStateMapper.toJson(providerState));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    public AccessMeansOrStepDTO createAccessMeans(N26GroupHttpClient httpClient,
                                                  UrlCreateAccessMeansRequest urlCreateAccessMeansRequest,
                                                  String role) {
        String authorizationCode = UriComponentsBuilder.fromUriString(urlCreateAccessMeansRequest.getRedirectUrlPostedBackFromSite())
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(CODE);

        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing authorization code");
        }
        try {
            N26GroupProviderState providerState = providerStateMapper.fromJson(urlCreateAccessMeansRequest.getProviderState());

            MultiValueMap<String, String> body = createBodyForTokenGrant(
                    authorizationCode,
                    urlCreateAccessMeansRequest.getBaseClientRedirectUrl(),
                    providerState);
            TokenResponse tokenResponse = httpClient.postAccessToken(properties.getTokenEndpoint(), body, role);
            validateTokenResponse(tokenResponse);
            providerState.setTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());

            ConsentCreationRequest consentCreationRequest = ConsentCreationRequest.builder()
                    .access(new Access(ALL_ACCOUNTS))
                    .recurringIndicator(true)
                    .validUntil(LocalDate.now(clock).plusDays(89).toString())
                    .frequencyPerDay(4)
                    .build();

            ConsentCreationResponse consentCreationResponse = httpClient.postConsentCreation(consentCreationRequest, providerState);
            String consentId = consentCreationResponse.getConsentId();
            providerState.setConsentId(consentId);

            consentStatusPoller.pollForConsentStatus(httpClient, providerState);
            return new AccessMeansOrStepDTO(providerStateMapper.toAccessMeansDTO(urlCreateAccessMeansRequest.getUserId(), providerState, tokenResponse));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    public AccessMeansDTO refreshAccessToken(N26GroupHttpClient httpClient,
                                             AccessMeansDTO accessMeansDTO,
                                             String role) throws TokenInvalidException {
        N26GroupProviderState providerState = providerStateMapper.fromJson(accessMeansDTO.getAccessMeans());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, REFRESH_TOKEN);
        body.add(REFRESH_TOKEN, providerState.getRefreshToken());

        TokenResponse tokenResponse = httpClient.postRefreshToken(properties.getTokenEndpoint(), body, role);
        validateTokenResponse(tokenResponse);
        providerState.setTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
        return providerStateMapper.toAccessMeansDTO(accessMeansDTO, providerState, tokenResponse);
    }

    public void deleteConsent(N26GroupHttpClient httpClient,
                              AccessMeansDTO accessMeansDTO) throws TokenInvalidException {
        N26GroupProviderState providerState = providerStateMapper.fromJson(accessMeansDTO.getAccessMeans());
        try {
            httpClient.deleteConsent(providerState);
        } catch (RuntimeException ex) {
            throw new TokenInvalidException("Something went wrong while trying to delete consent");
        }
    }

    private MultiValueMap<String, String> createBodyForTokenGrant(String authorizationCode,
                                                                  String redirectUrl,
                                                                  N26GroupProviderState providerState) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, AUTHORIZATION_CODE);
        body.add(CODE, authorizationCode);
        body.add(REDIRECT_URI, redirectUrl);
        body.add(CODE_VERIFIER, providerState.getCodeVerifier());
        body.add(REQUEST_ID, providerState.getRequestId());
        return body;
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
