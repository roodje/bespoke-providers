package com.yolt.providers.commerzbankgroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.commerzbankgroup.common.authentication.CommerzbankGroupAccessMeans;
import com.yolt.providers.commerzbankgroup.common.authentication.CommerzbankGroupAuthenticationService;
import com.yolt.providers.commerzbankgroup.common.authentication.CommerzbankGroupProviderState;
import com.yolt.providers.commerzbankgroup.common.authentication.LoginNotFoundCommerzbankException;
import com.yolt.providers.commerzbankgroup.common.authmeans.CommerzbankGroupAuthenticationMeansFactory;
import com.yolt.providers.commerzbankgroup.common.data.service.CommerzbankGroupFetchDataService;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CommerzbankGroupUrlDataProvider implements UrlDataProvider {

    private final CommerzbankGroupAuthenticationMeansFactory commerzbankGroupAuthenticationMeans;
    private final ProviderVersion providerVersion;
    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final CommerzbankBaseProperties commerzbankBaseProperties;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final CommerzbankGroupAuthenticationService commerzbankGroupAuthenticationService;
    private final CommerzbankGroupFetchDataService fetchDataService;


    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        var authMeans = commerzbankGroupAuthenticationMeans.toAuthenticationMeans(urlGetLogin.getAuthenticationMeans());
        var callContext = new CallContext(
                urlGetLogin::getRestTemplateManager,
                urlGetLogin::getPsuIpAddress,
                () -> authMeans,
                commerzbankBaseProperties,
                meterRegistry,
                providerIdentifier,
                objectMapper
        );

        var loginInfo = commerzbankGroupAuthenticationService.getLoginInfo(
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getState(),
                authMeans.getOrganizationIdentifier(),
                callContext::createApiClient);
        var providerState = new CommerzbankGroupProviderState(loginInfo.codeVerifier(), loginInfo.consentId());
        var serializedProviderState = callContext.serializeState(providerState, () -> new LoginNotFoundCommerzbankException("Cannot serialize state!"));
        return new RedirectStep(loginInfo.authorizationUrl(), null, serializedProviderState);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        var authMeans = commerzbankGroupAuthenticationMeans.toAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans());
        var callContext = new CallContext(
                urlCreateAccessMeans::getRestTemplateManager,
                urlCreateAccessMeans::getPsuIpAddress,
                () -> authMeans,
                commerzbankBaseProperties,
                meterRegistry,
                providerIdentifier,
                objectMapper
        );
        var commerzbankGroupProviderState = callContext.deserializeState(urlCreateAccessMeans.getProviderState(),
                CommerzbankGroupProviderState.class,
                () -> new GetAccessTokenFailedException("Cannot deserialize providerState!"));
        var newAccessMeans = commerzbankGroupAuthenticationService.createNewAccessMeans(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite(),
                urlCreateAccessMeans.getBaseClientRedirectUrl(),
                urlCreateAccessMeans.getState(),
                commerzbankGroupProviderState,
                callContext::createApiClient
        );

        var commerzbankGroupAccessMeans = new CommerzbankGroupAccessMeans(newAccessMeans.accessToken(), newAccessMeans.refreshToken(), commerzbankGroupProviderState.consentId());
        var accessMeansAsString = callContext.serializeState(commerzbankGroupAccessMeans, () -> new GetAccessTokenFailedException("Cannot serialize Access Means!"));

        return new AccessMeansOrStepDTO(new AccessMeansDTO(urlCreateAccessMeans.getUserId(), accessMeansAsString, Date.from(Instant.now(clock)),
                Date.from(Instant.now(clock).plusSeconds(newAccessMeans.expiresIn()))));
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        var authMeans = commerzbankGroupAuthenticationMeans.toAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans());
        var urlRefreshAccessMeansRequestCallContext = new CallContext(
                urlRefreshAccessMeans::getRestTemplateManager,
                urlRefreshAccessMeans::getPsuIpAddress,
                () -> authMeans,
                commerzbankBaseProperties,
                meterRegistry,
                providerIdentifier,
                objectMapper
        );
        var accessMeans = urlRefreshAccessMeans.getAccessMeans();
        var commerzbankGroupAccessMeans = urlRefreshAccessMeansRequestCallContext.deserializeState(
                accessMeans.getAccessMeans(),
                CommerzbankGroupAccessMeans.class,
                () -> new GetAccessTokenFailedException("Refresh token failed, Cannot deserialize AccessMeans"));
        var accessAndRefreshToken = commerzbankGroupAuthenticationService.refreshAccessMeans(
                commerzbankGroupAccessMeans.refreshToken(),
                urlRefreshAccessMeansRequestCallContext::createApiClient);
        var updatedCommerzbankGroupAccessMeans = new CommerzbankGroupAccessMeans(accessAndRefreshToken.accessToken(), commerzbankGroupAccessMeans.refreshToken(), commerzbankGroupAccessMeans.consentId());
        var serializedAccessMeans = urlRefreshAccessMeansRequestCallContext.serializeState(updatedCommerzbankGroupAccessMeans,
                () -> new GetAccessTokenFailedException("Refresh token failed, Cannot deserialize AccessMeans"));

        return new AccessMeansDTO(accessMeans.getUserId(), serializedAccessMeans, Date.from(Instant.now(clock)),
                Date.from(Instant.now(clock).plusSeconds(accessAndRefreshToken.expiresIn())));
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws TokenInvalidException {
        var authMeans = commerzbankGroupAuthenticationMeans.toAuthenticationMeans(urlFetchData.getAuthenticationMeans());
        var urlFetchDataContext = new CallContext(
                urlFetchData::getRestTemplateManager,
                urlFetchData::getPsuIpAddress,
                () -> authMeans,
                commerzbankBaseProperties,
                meterRegistry,
                providerIdentifier,
                objectMapper
        );
        var accessMeans = urlFetchData.getAccessMeans();
        var commerzbankGroupAccessMeans = urlFetchDataContext.deserializeState(
                accessMeans.getAccessMeans(),
                CommerzbankGroupAccessMeans.class,
                () -> new GetAccessTokenFailedException("Fetch data failed, Cannot deserialize AccessMeans"));

        return fetchDataService.fetchDataSince(commerzbankGroupAccessMeans.accessToken(), commerzbankGroupAccessMeans.consentId(), urlFetchData.getTransactionsFetchStartTime(), urlFetchDataContext::createApiClient);
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentifierDisplayName;
    }

    @Override
    public ProviderVersion getVersion() {
        return providerVersion;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return commerzbankGroupAuthenticationMeans.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return commerzbankGroupAuthenticationMeans.getTransportKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
