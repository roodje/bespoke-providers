package com.yolt.providers.monorepogroup.atruviagroup.common;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeansFactory;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.AtruviaEmbeddedFlowProcess;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.StepFactory;
import com.yolt.providers.monorepogroup.atruviagroup.common.service.fetchdata.AtruviaGroupFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class AtruviaGroupDataProvider implements UrlDataProvider {

    private final String providerIdentifierDisplayName;
    private final String providerIdentifier;
    private final ProviderVersion providerVersion;
    private final AtruviaEmbeddedFlowProcess embeddedFlowProcess;
    private final AtruviaGroupAuthenticationMeansFactory authenticationMeansFactory;
    private final StepFactory stepFactory;
    private final AtruviaGroupFetchDataService fetchDataService;

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        var stepOutcome = embeddedFlowProcess.initiateProcess();
        return stepFactory.toStep(stepOutcome);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        var nextStep = embeddedFlowProcess.getNextStep(stepFactory.createProcessStepData(urlCreateAccessMeans));
        return stepFactory.toAccessMeansOrStepDTO(nextStep);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        throw new TokenInvalidException("Not supported in Embedded flow!");
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        var authMeans = authenticationMeansFactory.toAuthenticationMeans(request.getAuthenticationMeans());
        return fetchDataService.fetchAccountsAndTransactions(
                authMeans,
                request.getAccessMeans(),
                request.getRestTemplateManager(),
                request.getPsuIpAddress(),
                request.getTransactionsFetchStartTime(),
                request.getSigner());
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
        return authenticationMeansFactory.getTypedAuthenticationMeans();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return authenticationMeansFactory.getSigningKeyRequirements();
    }
}
