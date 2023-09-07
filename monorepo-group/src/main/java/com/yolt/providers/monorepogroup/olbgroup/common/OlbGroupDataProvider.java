package com.yolt.providers.monorepogroup.olbgroup.common;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeansProducer;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.ProviderIdentification;
import com.yolt.providers.monorepogroup.olbgroup.common.service.authorization.OlbGroupAuthorizationService;
import com.yolt.providers.monorepogroup.olbgroup.common.service.fetchdata.OlbGroupFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class OlbGroupDataProvider implements UrlDataProvider {

    private final ProviderIdentification providerIdentification;
    private final OlbGroupAuthenticationMeansProducer authenticationMeansProducer;
    private final OlbGroupAuthorizationService authorizationService;
    private final OlbGroupFetchDataService fetchDataService;

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        var authMeans = authenticationMeansProducer.createAuthenticationMeans(request.getAuthenticationMeans(), getProviderIdentifier());
        return fetchDataService.fetchAccountsAndTransactions(
                authMeans,
                request.getAccessMeans(),
                request.getRestTemplateManager(),
                request.getPsuIpAddress(),
                request.getTransactionsFetchStartTime());
    }

    @Override
    public FormStep getLoginInfo(UrlGetLoginRequest request) {
        return authorizationService.createFormStepToRetrievePsuId();
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest request) {
        var authMeans = authenticationMeansProducer.createAuthenticationMeans(request.getAuthenticationMeans(), getProviderIdentifier());
        if (authorizationService.isFormStep(request.getFilledInUserSiteFormValues())) {
            return authorizationService.createRedirectStep(
                    authMeans,
                    request.getRestTemplateManager(),
                    request.getBaseClientRedirectUrl(),
                    request.getState(),
                    request.getPsuIpAddress(),
                    request.getFilledInUserSiteFormValues());
        }
        return authorizationService.createAccessMeans(
                request.getRedirectUrlPostedBackFromSite(),
                request.getProviderState(),
                request.getUserId());
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        throw new TokenInvalidException("Refresh token flow is not supported");
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest request) throws TokenInvalidException {
        var authMeans = authenticationMeansProducer.createAuthenticationMeans(request.getAuthenticationMeans(), getProviderIdentifier());
        authorizationService.deleteConsent(authMeans, request.getAccessMeans(), request.getRestTemplateManager());
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
        return authenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authenticationMeansProducer.getTransportKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
