package com.yolt.providers.gruppocedacri.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.gruppocedacri.common.autoonboarding.GruppoCedacriAutoOnboardingService;
import com.yolt.providers.gruppocedacri.common.config.ProviderIdentification;
import com.yolt.providers.gruppocedacri.common.dto.consent.ConsentResponse;
import com.yolt.providers.gruppocedacri.common.dto.token.TokenResponse;
import com.yolt.providers.gruppocedacri.common.http.GruppoCedacriHttpClient;
import com.yolt.providers.gruppocedacri.common.http.GruppoCedacriHttpClientFactory;
import com.yolt.providers.gruppocedacri.common.service.GruppoCedacriAuthorizationService;
import com.yolt.providers.gruppocedacri.common.service.GruppoCedacriFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;
import static com.yolt.providers.gruppocedacri.common.GruppoCedacriAuthenticationMeans.CLIENT_ID_NAME;
import static com.yolt.providers.gruppocedacri.common.GruppoCedacriAuthenticationMeans.CLIENT_SECRET_NAME;

@RequiredArgsConstructor
public class GruppoCedacriDataProviderV1 implements UrlDataProvider, AutoOnboardingProvider {

    private static final String FAKE_AUTHORIZATION_TOKEN = "fakeAuthorizationToken";

    private final ProviderIdentification providerIdentification;
    private final GruppoCedacriAuthorizationService authorizationService;
    private final GruppoCedacriFetchDataService fetchDataService;
    private final GruppoCedacriAutoOnboardingService autoOnboardingService;
    private final GruppoCedacriHttpClientFactory httpClientFactory;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        GruppoCedacriAuthenticationMeans authenticationMeans = GruppoCedacriAuthenticationMeans.fromAuthenticationMeans(
                urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());
        GruppoCedacriHttpClient httpClient = createHttpClient(urlGetLogin.getRestTemplateManager(), authenticationMeans);
        String authorizationUrl = authorizationService.getAuthorizationUrl(httpClient,
                FAKE_AUTHORIZATION_TOKEN,
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getPsuIpAddress(),
                urlGetLogin.getState(),
                authenticationMeans.getClientId());

        return new RedirectStep(authorizationUrl, null, null);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return StringUtils.hasText(urlCreateAccessMeans.getProviderState())
                ? returnAccessMeans(urlCreateAccessMeans.getProviderState())
                : getAccessTokenAndCreateConsent(urlCreateAccessMeans);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        // Gruppo Cedacri does not support refresh token. An access token is simply valid for 90 days. If expired, the user needs to relogin.
        throw new TokenInvalidException();
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        GruppoCedacriAuthenticationMeans authenticationMeans = GruppoCedacriAuthenticationMeans.fromAuthenticationMeans(
                urlOnUserSiteDeleteRequest.getAuthenticationMeans(), getProviderIdentifier());
        GruppoCedacriHttpClient httpClient = createHttpClient(urlOnUserSiteDeleteRequest.getRestTemplateManager(), authenticationMeans);

        GruppoCedacriAccessMeans accessMeans = deserializeGruppoCedacriAccessMeans(urlOnUserSiteDeleteRequest.getAccessMeans().getAccessMeans());
        authorizationService.deleteConsent(httpClient, accessMeans);
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        GruppoCedacriAuthenticationMeans authenticationMeans = GruppoCedacriAuthenticationMeans.fromAuthenticationMeans(
                urlFetchData.getAuthenticationMeans(), getProviderIdentifier());
        GruppoCedacriHttpClient httpClient = createHttpClient(urlFetchData.getRestTemplateManager(), authenticationMeans);

        return fetchDataService.fetchData(httpClient,
                deserializeGruppoCedacriAccessMeans(urlFetchData.getAccessMeans().getAccessMeans()),
                urlFetchData.getTransactionsFetchStartTime(),
                urlFetchData.getPsuIpAddress(),
                providerIdentification.getProviderDisplayName());
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return GruppoCedacriAuthenticationMeans.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return GruppoCedacriAuthenticationMeans.getTransportKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public final String getProviderIdentifier() {
        return providerIdentification.getProviderIdentifier();
    }

    @Override
    public final String getProviderIdentifierDisplayName() {
        return providerIdentification.getProviderDisplayName();
    }

    @Override
    public final ProviderVersion getVersion() {
        return providerIdentification.getProviderVersion();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = new HashMap<>();
        autoConfiguredMeans.put(CLIENT_ID_NAME, CLIENT_ID_STRING);
        autoConfiguredMeans.put(CLIENT_SECRET_NAME, CLIENT_SECRET_STRING);
        return autoConfiguredMeans;
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest request) {
        Map<String, BasicAuthenticationMean> authenticationMeansMap = request.getAuthenticationMeans();
        GruppoCedacriAuthenticationMeans authenticationMeans = GruppoCedacriAuthenticationMeans
                .fromAutoOnboardingAuthenticationMeans(authenticationMeansMap, providerIdentification.getProviderIdentifier());
        GruppoCedacriHttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authenticationMeans);
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeansMap);

        autoOnboardingService.register(httpClient, request)
                .ifPresent(clientRegistration -> {
                    BasicAuthenticationMean clientId = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                            clientRegistration.getAisp().getClientId());
                    BasicAuthenticationMean clientSecret = new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(),
                            clientRegistration.getAisp().getClientSecret());
                    mutableMeans.put(CLIENT_ID_NAME, clientId);
                    mutableMeans.put(CLIENT_SECRET_NAME, clientSecret);
                });

        return mutableMeans;
    }

    private GruppoCedacriHttpClient createHttpClient(RestTemplateManager restTemplateManager, GruppoCedacriAuthenticationMeans authenticationMeans) {
        return httpClientFactory.createHttpClient(authenticationMeans, restTemplateManager, getProviderIdentifier());
    }

    private AccessMeansOrStepDTO getAccessTokenAndCreateConsent(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        GruppoCedacriAuthenticationMeans authenticationMeans = GruppoCedacriAuthenticationMeans.fromAuthenticationMeans(
                urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        GruppoCedacriHttpClient httpClient = createHttpClient(urlCreateAccessMeans.getRestTemplateManager(), authenticationMeans);

        TokenResponse tokenResponse = authorizationService.getAccessToken(httpClient, urlCreateAccessMeans.getRedirectUrlPostedBackFromSite(),
                urlCreateAccessMeans.getBaseClientRedirectUrl(), authenticationMeans.getClientId(), authenticationMeans.getClientSecret());

        ConsentResponse consentResponse = authorizationService.createConsent(httpClient, tokenResponse.getAccessToken(),
                urlCreateAccessMeans.getBaseClientRedirectUrl(), urlCreateAccessMeans.getPsuIpAddress(), urlCreateAccessMeans.getState());

        String serializedAccessMeans = serializeGruppoCedacriAccessMeans(tokenResponse, consentResponse.getConsentId());

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                urlCreateAccessMeans.getUserId(),
                serializedAccessMeans,
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(tokenResponse.getExpiresIn()))
        );

        return new AccessMeansOrStepDTO(new RedirectStep(consentResponse.getScaRedirect(), null, serializeAccessMeansDTO(accessMeansDTO)));
    }

    private AccessMeansOrStepDTO returnAccessMeans(String providerState) {
        try {
            AccessMeansDTO accessMeansDTO = deserializeAccessMeansDTO(providerState);
            return new AccessMeansOrStepDTO(accessMeansDTO);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Got error on get access token step.");
        }
    }

    private String serializeGruppoCedacriAccessMeans(TokenResponse tokenResponse, String consentId) {
        try {
            return objectMapper.writeValueAsString(new GruppoCedacriAccessMeans(tokenResponse, consentId));
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize access token");
        }
    }

    private GruppoCedacriAccessMeans deserializeGruppoCedacriAccessMeans(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, GruppoCedacriAccessMeans.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access token");
        }
    }

    private String serializeAccessMeansDTO(AccessMeansDTO accessMeansDTO) {
        try {
            return objectMapper.writeValueAsString(accessMeansDTO);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize access means");
        }
    }

    private AccessMeansDTO deserializeAccessMeansDTO(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, AccessMeansDTO.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access means");
        }
    }
}
