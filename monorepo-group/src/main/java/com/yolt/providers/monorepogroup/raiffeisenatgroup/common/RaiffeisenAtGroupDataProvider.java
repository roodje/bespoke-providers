package com.yolt.providers.monorepogroup.raiffeisenatgroup.common;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.auth.typedauthmeans.RaiffeisenAtGroupTypedAuthenticationMeansProducer;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.ProviderIdentification;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClientProducer;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.RaiffeisenAtGroupAuthMeansMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service.RaiffeisenAtGroupAuthorizationService;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service.RaiffeisenAtGroupDataMappingService;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service.RaiffeisenAtGroupFetchDataService;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service.RaiffeisenAtGroupRegistrationService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans.CLIENT_ID_NAME;

@RequiredArgsConstructor
public class RaiffeisenAtGroupDataProvider implements UrlDataProvider, AutoOnboardingProvider {

    private final ProviderIdentification providerIdentification;
    private final RaiffeisenAtGroupTypedAuthenticationMeansProducer typedAuthenticationMeansProducer;
    private final RaiffeisenAtGroupAuthMeansMapper authMeansMapper;
    private final RaiffeisenAtGroupHttpClientProducer httpClientProducer;
    private final RaiffeisenAtGroupAuthorizationService authorizationService;
    private final RaiffeisenAtGroupFetchDataService fetchDataService;
    private final RaiffeisenAtGroupDataMappingService mappingService;
    private final RaiffeisenAtGroupRegistrationService registrationService;
    private final ConsentValidityRules consentValidityRules;

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        RaiffeisenAtGroupAuthenticationMeans authenticationMeans = authMeansMapper.map(urlFetchData.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        RaiffeisenAtGroupHttpClient httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlFetchData.getRestTemplateManager());
        FetchDataResult fetchDataResult = fetchDataService.fetchData(httpClient,
                authenticationMeans,
                urlFetchData.getAccessMeans().getAccessMeans(),
                urlFetchData.getPsuIpAddress(),
                urlFetchData.getTransactionsFetchStartTime());
        return mappingService.mapToDateProviderResponse(fetchDataResult);
    }

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        RaiffeisenAtGroupAuthenticationMeans authenticationMeans = authMeansMapper.map(urlGetLogin.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        RaiffeisenAtGroupHttpClient httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlGetLogin.getRestTemplateManager());
        return authorizationService.getLoginInfo(httpClient, authenticationMeans, urlGetLogin.getBaseClientRedirectUrl(), urlGetLogin.getPsuIpAddress(), urlGetLogin.getState());
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        RaiffeisenAtGroupAuthenticationMeans authenticationMeans = authMeansMapper.map(urlCreateAccessMeans.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        RaiffeisenAtGroupHttpClient httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlCreateAccessMeans.getRestTemplateManager());
        return authorizationService.createNewAccessMeans(httpClient,
                authenticationMeans,
                urlCreateAccessMeans.getRedirectUrlPostedBackFromSite(),
                urlCreateAccessMeans.getUserId(),
                urlCreateAccessMeans.getPsuIpAddress(),
                urlCreateAccessMeans.getProviderState());
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        throw new TokenInvalidException("Refresh token not supported");
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        RaiffeisenAtGroupAuthenticationMeans authenticationMeans = authMeansMapper.map(urlOnUserSiteDeleteRequest.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        RaiffeisenAtGroupHttpClient httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlOnUserSiteDeleteRequest.getRestTemplateManager());
        authorizationService.deleteUserConsent(httpClient,
                authenticationMeans,
                urlOnUserSiteDeleteRequest.getPsuIpAddress(),
                urlOnUserSiteDeleteRequest.getExternalConsentId());
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentification.getProviderIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentification.getProviderDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return providerIdentification.getVersion();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedAuthenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return typedAuthenticationMeansProducer.getTransportKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return consentValidityRules;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return typedAuthenticationMeansProducer.getAutoconfigureTypedAuthenticationMeans();
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> basicAuthenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        RaiffeisenAtGroupAuthenticationMeans raiffeisenAtGroupAuthenticationMeans = authMeansMapper.mapForAutoonboarding(basicAuthenticationMeans, providerIdentification.getProviderIdentifier());
        RaiffeisenAtGroupHttpClient httpClient = httpClientProducer.createHttpClient(raiffeisenAtGroupAuthenticationMeans, urlAutoOnboardingRequest.getRestTemplateManager());
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(basicAuthenticationMeans);
        registrationService.register(httpClient)
                .ifPresent(registrationResponse -> {
                    BasicAuthenticationMean clientIdMeans = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), registrationResponse.getClientId());
                    mutableMeans.put(CLIENT_ID_NAME, clientIdMeans);
                });
        return mutableMeans;

    }
}
