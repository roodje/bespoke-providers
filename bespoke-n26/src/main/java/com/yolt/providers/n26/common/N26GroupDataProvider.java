package com.yolt.providers.n26.common;

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
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.n26.common.auth.N26GroupAuthenticationMeans;
import com.yolt.providers.n26.common.auth.N26GroupAuthenticationMeansProducer;
import com.yolt.providers.n26.common.http.N26GroupHttpClient;
import com.yolt.providers.n26.common.http.N26GroupHttpClientFactory;
import com.yolt.providers.n26.common.service.N26GroupAuthorizationService;
import com.yolt.providers.n26.common.service.N26GroupFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class N26GroupDataProvider implements UrlDataProvider {

    private static final String DEDICATED_AISP = "DEDICATED_AISP";

    private final N26GroupHttpClientFactory httpClientFactory;
    private final N26GroupAuthorizationService authorizationService;
    private final N26GroupFetchDataService fetchDataService;
    private final N26GroupAuthenticationMeansProducer authenticationMeansProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authenticationMeansProducer.getTransportKeyRequirements();
    }

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest request) {
        N26GroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        N26GroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return authorizationService.createRedirectStepToConsentPage(httpClient, request);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest request) {
        N26GroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        N26GroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return authorizationService.createAccessMeans(httpClient, request, DEDICATED_AISP);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        N26GroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        N26GroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return authorizationService.refreshAccessToken(httpClient, request.getAccessMeans(), DEDICATED_AISP);
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        N26GroupAuthenticationMeans authMeans = createAuthMeans(urlOnUserSiteDeleteRequest.getAuthenticationMeans());
        N26GroupHttpClient httpClient = createHttpClient(authMeans, urlOnUserSiteDeleteRequest.getRestTemplateManager());
        authorizationService.deleteConsent(httpClient, urlOnUserSiteDeleteRequest.getAccessMeans());
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        N26GroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        N26GroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return fetchDataService.fetchData(httpClient, request);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private N26GroupAuthenticationMeans createAuthMeans(Map<String, BasicAuthenticationMean> authMeans) {
        return authenticationMeansProducer.createAuthenticationMeans(authMeans, getProviderIdentifier());
    }

    private N26GroupHttpClient createHttpClient(N26GroupAuthenticationMeans authMeans,
                                                RestTemplateManager restTemplateManager) {
        return httpClientFactory.createHttpClient(authMeans, restTemplateManager, getProviderIdentifierDisplayName());
    }
}
