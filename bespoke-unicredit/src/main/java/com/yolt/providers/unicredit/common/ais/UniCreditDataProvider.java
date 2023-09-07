package com.yolt.providers.unicredit.common.ais;

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
import com.yolt.providers.unicredit.common.data.UniCreditFetchDataService;
import com.yolt.providers.unicredit.common.service.UniCreditAuthenticationMeansProducer;
import com.yolt.providers.unicredit.common.service.UniCreditAuthorizationService;
import com.yolt.providers.unicredit.common.service.UniCreditAutoOnboardingService;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class UniCreditDataProvider implements AutoOnboardingProvider, UrlDataProvider {

    private final UniCreditAuthenticationMeansProducer authenticationMeansService;
    private final UniCreditAutoOnboardingService autoOnboardingService;
    private final UniCreditAuthorizationService authorizationService;
    private final UniCreditFetchDataService fetchDataService;

    private final ProviderInfo providerInfo;

    @Override
    public String getProviderIdentifier() {
        return providerInfo.getIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerInfo.getDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return providerInfo.getVersion();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return authenticationMeansService.getAutoConfiguredMeans();
    }

    @SneakyThrows
    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        return autoOnboardingService.autoConfigureMeans(urlAutoOnboardingRequest, providerInfo);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authenticationMeansService.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authenticationMeansService.getTransportKeyRequirements();
    }

    @SneakyThrows
    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        return authorizationService.getLoginInfo(urlGetLogin, providerInfo);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return authorizationService.createNewAccessMeans(urlCreateAccessMeans, providerInfo);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        return authorizationService.refreshAccessMeans(urlRefreshAccessMeans, providerInfo);
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        return fetchDataService.fetchData(urlFetchData, providerInfo);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
