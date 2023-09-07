package com.yolt.providers.axabanque.common.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans;
import com.yolt.providers.axabanque.common.auth.http.client.AuthorizationHttpClient;
import com.yolt.providers.axabanque.common.auth.http.clientproducer.HttpClientProducer;
import com.yolt.providers.axabanque.common.auth.mapper.access.AccessMeansMapper;
import com.yolt.providers.axabanque.common.config.GroupProperties;
import com.yolt.providers.axabanque.common.model.external.AuthorizationResponse;
import com.yolt.providers.axabanque.common.model.external.ConsentResponse;
import com.yolt.providers.axabanque.common.model.external.Token;
import com.yolt.providers.axabanque.common.model.internal.AccessToken;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.axabanque.common.model.internal.GroupProviderState;
import com.yolt.providers.axabanque.common.pkce.PKCE;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;


@AllArgsConstructor
public class DefaultAuthenticationService implements AuthenticationService {
    public static final String GRANT_TYPE = "grant_type";
    public static final String SCOPE = "scope";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String CLIENT_ID = "client_id";
    public static final String STATE = "state";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String CODE = "code";
    public static final String CODE_CHALLENGE = "code_challenge";
    public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    public static final String CODE_VERIFIER = "code_verifier";

    private static final String AUTHORIZE_ENDPOINT = "/authorize/%s";

    private final Clock clock;
    private final PKCE pkce;
    private final HttpClientProducer restTemplateProducer;
    private final GroupProperties properties;
    private final ObjectMapper objectMapper;
    private final AccessMeansMapper accessMeansMapper;
    private final Function<Token, AccessToken> accessTokenMapper;
    private final Supplier<String> traceIdProducer;

    @Override
    public RedirectStep getLoginInfo(GroupAuthenticationMeans authMeans, String state, String baseClientRedirectUrl, String psuIpAddress, RestTemplateManager restTemplateManager) {
        try {
            AuthorizationHttpClient httpClient = restTemplateProducer.getAuthenticationHttpClient(authMeans.getTransportKeyId(), authMeans.getTlsCertificate(), restTemplateManager);
            String xRequestId = traceIdProducer.get();
            ConsentResponse consentResponseDTO = httpClient.initiateConsent(baseClientRedirectUrl, psuIpAddress, LocalDate.now(clock).plusDays(89), xRequestId);
            AuthorizationResponse authorizationResponseDto = httpClient.initiateAuthorizationResource(consentResponseDTO, xRequestId);
            OAuth2ProofKeyCodeExchange codeExchange = pkce.createRandomS256();
            String clientLoginUrl = getClientLoginUrl(authMeans.getClientId(), state, baseClientRedirectUrl,
                    codeExchange, consentResponseDTO, authorizationResponseDto);
            String providerState = serialize(new GroupProviderState(codeExchange.getCodeVerifier(), codeExchange.getCodeChallenge(), consentResponseDTO.getConsentId(), xRequestId, Instant.now(clock).toEpochMilli()));
            return new RedirectStep(clientLoginUrl, consentResponseDTO.getConsentId(), providerState);
        } catch (ProviderHttpStatusException | TokenInvalidException e) {
            throw new GetLoginInfoUrlFailedException("Failed to get authorization url", e);
        }
    }

    private String serialize(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }

    private String getClientLoginUrl(String clientId, String state, String baseClientRedirectUri, OAuth2ProofKeyCodeExchange codeExchange,
                                     ConsentResponse consentResponseDTO, AuthorizationResponse authorizationResponseDto) {
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(RESPONSE_TYPE, "code");
        requestPayload.add(CLIENT_ID, clientId);
        requestPayload.add(SCOPE, String.format("AIS:%s", consentResponseDTO.getConsentId()));
        requestPayload.add(STATE, state);
        requestPayload.add(REDIRECT_URI, baseClientRedirectUri);
        requestPayload.add(CODE_CHALLENGE, codeExchange.getCodeChallenge());
        requestPayload.add(CODE_CHALLENGE_METHOD, codeExchange.getCodeChallengeMethod());

        return UriComponentsBuilder.fromHttpUrl(
                properties.getAuthorizationBaseUrl() + String.format(AUTHORIZE_ENDPOINT, authorizationResponseDto.getAuthorisationIds().get(0)))
                .queryParams(requestPayload)
                .build()
                .toString();
    }

    @Override
    public AccessMeansDTO createAccessMeans(GroupAuthenticationMeans authMeans, String serializedProviderState, UUID userId,
                                            String baseRedirectUri, String redirectUriPostedBackFromSite, RestTemplateManager restTemplateManager) {
        AuthorizationHttpClient httpClient = restTemplateProducer.getAuthenticationHttpClient(authMeans.getTransportKeyId(), authMeans.getTlsCertificate(), restTemplateManager);
        String authorizationCode = getAuthorizationCode(redirectUriPostedBackFromSite);
        GroupProviderState providerState = getAxaGroupProviderState(serializedProviderState);
        try {
            Token token = httpClient.createToken(authMeans.getClientId(), baseRedirectUri, authorizationCode, providerState.getCodeVerifier());
            GroupAccessMeans axaAccessMeans = accessMeansMapper.mapToAccessMeans(baseRedirectUri, providerState, accessTokenMapper.apply(token));
            return new AccessMeansDTO(userId, serialize(axaAccessMeans), new Date(), new Date(token.getExpiresIn()));
        } catch (ProviderHttpStatusException | TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Failed to get access token", e);
        }
    }

    private String getAuthorizationCode(String callbackUrl) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(CODE);
    }

    private GroupProviderState getAxaGroupProviderState(String serializedProviderState) {
        try {
            return objectMapper.readValue(serializedProviderState, GroupProviderState.class);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to deserialize provider state");
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(GroupAuthenticationMeans authMeans, GroupAccessMeans accessMeans, UUID userId, RestTemplateManager restTemplateManager) throws TokenInvalidException {

        AuthorizationHttpClient httpClient = restTemplateProducer.getAuthenticationHttpClient(authMeans.getTransportKeyId(), authMeans.getTlsCertificate(), restTemplateManager);
        GroupProviderState providerState = accessMeans.getProviderState();
        Token token = httpClient.refreshToken(authMeans.getClientId(), accessMeans.getBaseRedirectUri(), providerState.getCode(), providerState.getCodeVerifier(), accessMeans.getAccessToken().getRefreshToken());
        AccessToken refreshedAccessToken = accessTokenMapper.apply(token);
        refreshedAccessToken.setRefreshToken(accessMeans.getAccessToken().getRefreshToken());
        String serializedNewAccessMeans = serialize(accessMeans.withAccessToken(refreshedAccessToken));
        return new AccessMeansDTO(userId, serializedNewAccessMeans, new Date(), new Date(token.getExpiresIn()));
    }

    @Override
    public void deleteConsent(GroupAuthenticationMeans authMeans, GroupAccessMeans accessMeans, RestTemplateManager restTemplateManager) throws TokenInvalidException {
        AuthorizationHttpClient httpClient = restTemplateProducer.getAuthenticationHttpClient(authMeans.getTransportKeyId(), authMeans.getTlsCertificate(), restTemplateManager);
        GroupProviderState providerState = accessMeans.getProviderState();
        httpClient.deleteConsent(providerState.getTraceId(), providerState.getConsentId());
    }
}
