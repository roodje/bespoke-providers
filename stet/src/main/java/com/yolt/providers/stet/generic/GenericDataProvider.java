package com.yolt.providers.stet.generic;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansOrStepRequest;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansRequest;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;
import com.yolt.providers.stet.generic.service.fetchdata.request.FetchDataRequest;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class GenericDataProvider implements UrlDataProvider {

    private final AuthenticationMeansSupplier authMeansSupplier;
    private final HttpClientFactory httpClientFactory;
    private final AuthorizationService authorizationService;
    private final FetchDataService fetchDataService;
    private final ProviderStateMapper providerStateMapper;
    private final ConsentValidityRules consentValidityRules;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authMeansSupplier.getTypedAuthMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authMeansSupplier.getTransportKeyRequirements();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return authMeansSupplier.getSigningKeyRequirements();
    }

    @Override
    public Step getLoginInfo(UrlGetLoginRequest request) {
        DefaultAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        return authorizationService.getStep(StepRequest.baseStepRequest(
                authMeans,
                request.getBaseClientRedirectUrl(),
                request.getState()
        ));
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest request) {
        DefaultAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        try {
            DataProviderState providerState = providerStateMapper.mapToDataProviderState(request.getProviderState());

            HttpClient httpClient = null;
            if (providerState.hasRegion()) {
                httpClient = createHttpClient(request.getRestTemplateManager(), authMeans, providerState.getRegion().getBaseUrl());
            }
            AccessMeansOrStepRequest accessMeansOrStepRequest = new AccessMeansOrStepRequest(
                    request.getProviderState(),
                    authMeans,
                    request.getRedirectUrlPostedBackFromSite(),
                    request.getBaseClientRedirectUrl(),
                    request.getUserId(),
                    request.getState(),
                    request.getFilledInUserSiteFormValues(),
                    request.getSigner());

            return authorizationService.createAccessMeansOrGetStep(httpClient, accessMeansOrStepRequest);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e.getMessage());
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        DefaultAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        DataProviderState providerState = providerStateMapper.mapToDataProviderState(request.getAccessMeans().getAccessMeans());
        HttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authMeans, providerState.getRegion().getBaseUrl());

        AccessMeansRequest accessMeansRequest = new AccessMeansRequest(authMeans, request.getAccessMeans(), providerState, request.getSigner());
        return authorizationService.refreshAccessMeans(httpClient, accessMeansRequest);
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        DefaultAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        DataProviderState providerState = providerStateMapper.mapToDataProviderState(request.getAccessMeans().getAccessMeans());
        HttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authMeans, providerState.getRegion().getBaseUrl());

        FetchDataRequest fetchDataRequest = new FetchDataRequest(
                providerState,
                request.getTransactionsFetchStartTime(),
                request.getSigner(),
                authMeans,
                request.getPsuIpAddress());

        return fetchDataService.getAccountsAndTransactions(httpClient, fetchDataRequest);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return consentValidityRules != null ? consentValidityRules : ConsentValidityRules.EMPTY_RULES_SET;
    }

    protected DefaultAuthenticationMeans createAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans) {
        return authMeansSupplier.getAuthMeans(basicAuthMeans, getProviderIdentifier());
    }

    protected HttpClient createHttpClient(RestTemplateManager restTemplateManager, DefaultAuthenticationMeans authMeans, String baseUrl) {
        return httpClientFactory.createHttpClient(restTemplateManager, authMeans, baseUrl, getProviderIdentifierDisplayName());
    }
}
