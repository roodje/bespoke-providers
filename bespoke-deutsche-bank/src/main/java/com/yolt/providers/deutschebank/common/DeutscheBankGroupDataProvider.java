package com.yolt.providers.deutschebank.common;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeans;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducer;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClient;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClientFactory;
import com.yolt.providers.deutschebank.common.service.authorization.DeutscheBankGroupAuthorizationService;
import com.yolt.providers.deutschebank.common.service.fetchdata.DeutscheBankGroupFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class DeutscheBankGroupDataProvider implements UrlDataProvider {

    private final DeutscheBankGroupHttpClientFactory httpClientFactory;
    private final DeutscheBankGroupAuthorizationService authorizationService;
    private final DeutscheBankGroupFetchDataService fetchDataService;
    private final DeutscheBankGroupAuthenticationMeansProducer authenticationMeansProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authenticationMeansProducer.getTransportKeyRequirements();
    }

    @Override
    public FormStep getLoginInfo(UrlGetLoginRequest request) {
        return authorizationService.createFormStepToRetrievePsuId();
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest request) {
        DeutscheBankGroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        DeutscheBankGroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        if (authorizationService.isFormStep(request.getFilledInUserSiteFormValues())) {
            return authorizationService.createRedirectStepToInitiatedConsentPage(httpClient, request);
        }
        return authorizationService.createAccessMeans(request);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        throw new TokenInvalidException("Refresh token flow is not supported");
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        DeutscheBankGroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        DeutscheBankGroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        return fetchDataService.fetchAccountsAndTransactions(getProviderIdentifierDisplayName(), httpClient, request);
    }

    protected DeutscheBankGroupAuthenticationMeans createAuthMeans(Map<String, BasicAuthenticationMean> authMeans) {
        return authenticationMeansProducer.createAuthenticationMeans(authMeans, getProviderIdentifier());
    }

    protected DeutscheBankGroupHttpClient createHttpClient(DeutscheBankGroupAuthenticationMeans authMeans,
                                                           RestTemplateManager restTemplateManager) {
        return httpClientFactory.createHttpClient(authMeans, restTemplateManager, getProviderIdentifierDisplayName());
    }
}
