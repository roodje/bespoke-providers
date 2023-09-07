package com.yolt.providers.monorepogroup.handelsbankengroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeans;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeansProducer;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthService;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.ConsentData;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.ProviderIdentification;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.http.HandelsbankenGroupHttpClient;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.http.HandelsbankenGroupHttpClientFactory;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.onboarding.HandelsbankenGroupOnboardingService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.TPP_ID;
import static com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeansProducerV1.CLIENT_ID_NAME;
import static com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeansProducerV1.TPP_ID_NAME;

@RequiredArgsConstructor
public class HandelsbankenGroupDataProvider implements UrlDataProvider, AutoOnboardingProvider {

    private final ProviderIdentification providerIdentification;
    private final HandelsbankenGroupAuthMeansProducer authMeansProducer;
    private final HandelsbankenGroupOnboardingService handelsbankenGroupOnboardingService;
    private final HandelsbankenGroupAuthService authService;
    private final HandelsbankenGroupHttpClientFactory httpClientFactory;
    private final HandelsbankenGroupDateConverter dateConverter;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authMeansProducer.getTransportKeyRequirements();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return authMeansProducer.getAutoConfigureMeans();
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {

        Map<String, BasicAuthenticationMean> basicAuthenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        HandelsbankenGroupAuthMeans authMeans = authMeansProducer.createAuthenticationMeans(basicAuthenticationMeans,
                providerIdentification.providerIdentifier());
        HandelsbankenGroupHttpClient httpClient = httpClientFactory.createHttpClient(authMeans,
                urlAutoOnboardingRequest.getRestTemplateManager(), getProviderIdentifier());
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(basicAuthenticationMeans);
        HandelsbankenGroupOnboardingService.RegistrationData registrationData = handelsbankenGroupOnboardingService.registerAndSubscribe(httpClient,
                authMeans,
                urlAutoOnboardingRequest.getRedirectUrls().get(0),
                providerIdentification.providerDisplayName());
        BasicAuthenticationMean tppIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), registrationData.tppId());
        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(TPP_ID.getType(), registrationData.clientId());
        mutableMeans.put(TPP_ID_NAME, tppIdMean);
        mutableMeans.put(CLIENT_ID_NAME, clientIdMean);
        return mutableMeans;
    }

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        HandelsbankenGroupAuthMeans authMeans = authMeansProducer.createAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());
        HandelsbankenGroupHttpClient httpClient = httpClientFactory.createHttpClient(authMeans,
                urlGetLogin.getRestTemplateManager(), getProviderIdentifier());
        ConsentData consentData = authService.generateConsent(httpClient,
                authMeans.getTppId(),
                authMeans.getClientId(),
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getState());
        return new RedirectStep(consentData.consentPageUrl(), null, consentData.consentId());
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        HandelsbankenGroupAuthMeans authMeans = authMeansProducer.createAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        HandelsbankenGroupHttpClient httpClient = httpClientFactory.createHttpClient(authMeans,
                urlCreateAccessMeans.getRestTemplateManager(), getProviderIdentifier());

        HandelsbankenGroupAccessMeans accessMeans = authService.createAccessMeans(httpClient,
                authMeans.getClientId(),
                urlCreateAccessMeans.getProviderState(),
                extractAuthCode(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite()),
                urlCreateAccessMeans.getBaseClientRedirectUrl());

        return new AccessMeansOrStepDTO(new AccessMeansDTO(urlCreateAccessMeans.getUserId(),
                toJson(accessMeans),
                dateConverter.toDate(LocalDate.now(clock)),
                dateConverter.toDate(LocalDate.now(clock).plusDays(89))));
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        HandelsbankenGroupAuthMeans authMeans = authMeansProducer.createAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        HandelsbankenGroupHttpClient httpClient = httpClientFactory.createHttpClient(authMeans,
                urlRefreshAccessMeans.getRestTemplateManager(), getProviderIdentifier());

        HandelsbankenGroupAccessMeans oldAccessMeans = fromJson(urlRefreshAccessMeans.getAccessMeans().getAccessMeans());

        HandelsbankenGroupAccessMeans newAccessMeans = authService.refreshAccessMeans(httpClient,
                authMeans.getClientId(),
                oldAccessMeans);

        return new AccessMeansDTO(urlRefreshAccessMeans.getAccessMeans().getUserId(),
                toJson(newAccessMeans),
                dateConverter.toDate(LocalDate.now(clock)),
                dateConverter.toDate(LocalDate.now(clock).plusDays(89)));
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        throw new NotImplementedException("To be implemented");
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentification.providerIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentification.providerDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return providerIdentification.version();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private String toJson(HandelsbankenGroupAccessMeans accessMeans) {
        try {
            return objectMapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }

    private HandelsbankenGroupAccessMeans fromJson(String accessMeans) {
        try {
            return objectMapper.readValue(accessMeans, HandelsbankenGroupAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to deserialize provider state");
        }
    }

    private String extractAuthCode(String redirectUrlPostedBackFromSite) {
        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        final String error = queryParams.get("error");
        if (StringUtils.isNotEmpty(error)) {
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectUrlPostedBackFromSite);
        }

        final String authorizationCode = queryParams.get("code");
        if (ObjectUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing data for key code.");
        }
        return authorizationCode;
    }
}
