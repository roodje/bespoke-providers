package com.yolt.providers.monorepogroup.chebancagroup;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupTypedAuthenticationMeansProducer;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.ProviderIdentification;
import com.yolt.providers.monorepogroup.chebancagroup.common.http.CheBancaGroupHttpClient;
import com.yolt.providers.monorepogroup.chebancagroup.common.http.DefaultCheBancaGroupHttpClientProducer;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.authmeans.CheBancaGroupAuthMeansMapper;
import com.yolt.providers.monorepogroup.chebancagroup.common.service.CheBancaGroupAuthorizationService;
import com.yolt.providers.monorepogroup.chebancagroup.common.service.CheBancaGroupDataMappingService;
import com.yolt.providers.monorepogroup.chebancagroup.common.service.ChebancaGroupFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CheBancaGroupDataProvider implements UrlDataProvider {

    private final ProviderIdentification providerIdentification;
    private final CheBancaGroupTypedAuthenticationMeansProducer typedAuthenticationMeansProducer;
    private final CheBancaGroupAuthMeansMapper authMeansMapper;
    private final DefaultCheBancaGroupHttpClientProducer httpClientProducer;
    private final CheBancaGroupAuthorizationService authorizationService;
    private final ChebancaGroupFetchDataService fetchDataService;
    private final CheBancaGroupDataMappingService mappingService;

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        CheBancaGroupAuthenticationMeans authenticationMeans = authMeansMapper.map(urlFetchData.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        CheBancaGroupHttpClient httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlFetchData.getRestTemplateManager());
        FetchDataResult fetchDataResult = fetchDataService.fetchData(
                urlFetchData,
                urlFetchData.getSigner(),
                httpClient,
                authenticationMeans,
                urlFetchData.getTransactionsFetchStartTime());
        return mappingService.mapToDateProviderResponse(fetchDataResult);
    }

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        CheBancaGroupAuthenticationMeans authenticationMeans = authMeansMapper.map(urlGetLogin.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        CheBancaGroupHttpClient httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlGetLogin.getRestTemplateManager());
        return authorizationService.getLoginInfo(httpClient, urlGetLogin.getSigner(), authenticationMeans, urlGetLogin.getBaseClientRedirectUrl(), urlGetLogin.getState());
    }

    @Override
    public void onUserSiteDelete(final UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        throw new NotImplementedException("TODO: UNKNOWN ENDPOINT DETAILS");
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        CheBancaGroupAuthenticationMeans authenticationMeans = authMeansMapper.map(urlCreateAccessMeans.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        CheBancaGroupHttpClient httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlCreateAccessMeans.getRestTemplateManager());
        return authorizationService.createNewAccessMeans(urlCreateAccessMeans.getSigner(), httpClient, authenticationMeans, urlCreateAccessMeans);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        CheBancaGroupAuthenticationMeans authenticationMeans = authMeansMapper.map(urlRefreshAccessMeans.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        CheBancaGroupHttpClient httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlRefreshAccessMeans.getRestTemplateManager());
        return authorizationService.refreshAccessMeans(urlRefreshAccessMeans.getSigner(), httpClient, authenticationMeans, urlRefreshAccessMeans);
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
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return typedAuthenticationMeansProducer.getSigningKeyRequirements();
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
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
