package com.yolt.providers.bancatransilvania.common;

import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeans;
import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeansProducer;
import com.yolt.providers.bancatransilvania.common.http.BancaTransilvaniaGroupHttpClient;
import com.yolt.providers.bancatransilvania.common.http.BancaTransilvaniaGroupHttpClientFactory;
import com.yolt.providers.bancatransilvania.common.service.BancaTransilvaniaGroupAuthorizationService;
import com.yolt.providers.bancatransilvania.common.service.BancaTransilvaniaGroupFetchDataService;
import com.yolt.providers.bancatransilvania.common.service.BancaTransilvaniaGroupRegistrationService;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class BancaTransilvaniaGroupDataProvider implements AutoOnboardingProvider, UrlDataProvider {

    private final BancaTransilvaniaGroupHttpClientFactory httpClientFactory;
    private final BancaTransilvaniaGroupRegistrationService registrationService;
    private final BancaTransilvaniaGroupAuthorizationService authorizationService;
    private final BancaTransilvaniaGroupFetchDataService fetchDataService;
    private final BancaTransilvaniaGroupAuthenticationMeansProducer authenticationMeansProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authenticationMeansProducer.getTransportKeyRequirements();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return authenticationMeansProducer.getAutoConfiguredMeans();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest request) {
        BancaTransilvaniaGroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        BancaTransilvaniaGroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return registrationService.register(httpClient, authMeans, request);
    }

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest request) {
        BancaTransilvaniaGroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        BancaTransilvaniaGroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return authorizationService.createRedirectStepToInitiatedConsentPage(httpClient, request, authMeans);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest request) {
        BancaTransilvaniaGroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        BancaTransilvaniaGroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return authorizationService.createAccessMeans(httpClient, authMeans, request);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        BancaTransilvaniaGroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        BancaTransilvaniaGroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return authorizationService.refreshAccessToken(httpClient, authMeans, request.getAccessMeans());
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        BancaTransilvaniaGroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        BancaTransilvaniaGroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return fetchDataService.fetchData(httpClient, request);
    }

    private BancaTransilvaniaGroupAuthenticationMeans createAuthMeans(Map<String, BasicAuthenticationMean> authMeans) {
        return authenticationMeansProducer.createAuthenticationMeans(authMeans, getProviderIdentifier());
    }
    
    private BancaTransilvaniaGroupHttpClient createHttpClient(BancaTransilvaniaGroupAuthenticationMeans authMeans, 
                                                              RestTemplateManager restTemplateManager) {
        return httpClientFactory.createHttpClient(authMeans, restTemplateManager, getProviderIdentifierDisplayName());
    }
}
