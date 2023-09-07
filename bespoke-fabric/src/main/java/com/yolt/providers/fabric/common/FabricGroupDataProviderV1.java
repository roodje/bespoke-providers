package com.yolt.providers.fabric.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.*;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.fabric.common.auth.AuthenticationService;
import com.yolt.providers.fabric.common.auth.FabricGroupAuthenticationMeans;
import com.yolt.providers.fabric.common.beanconfig.FabricGroupProperties;
import com.yolt.providers.fabric.common.fetchdata.DefaultFetchDataService;
import com.yolt.providers.fabric.common.http.FabricDefaultHttpClient;
import com.yolt.providers.fabric.common.http.FabricGroupHttpClientFactory;
import com.yolt.providers.fabric.common.model.GroupProviderState;
import com.yolt.providers.fabric.common.onboarding.FabricGroupOnboardingHttpClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yolt.providers.fabric.common.auth.FabricGroupAuthenticationMeans.*;
import static com.yolt.providers.fabric.common.onboarding.FabricGroupOnboardingHttpClient.createHttpClient;

@AllArgsConstructor
public class FabricGroupDataProviderV1 implements UrlDataProvider, AutoOnboardingProvider {
    private final FabricGroupProperties properties;
    private final AuthenticationService authenticationService;
    private final DefaultFetchDataService fetchDataService;
    private final ObjectMapper objectMapper;
    private final FabricGroupHttpClientFactory restTemplateProducer;
    @Getter
    private final String providerIdentifier;
    @Getter
    private final String providerIdentifierDisplayName;
    @Getter
    private final ProviderVersion version;

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        FabricGroupAuthenticationMeans authMeans = createAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), providerIdentifier);
        FabricDefaultHttpClient defaultHttpHttpClient = createDefaultHttpHttpClient(authMeans, urlGetLogin.getRestTemplateManager());
        try {
            return authenticationService.getConsentStep(defaultHttpHttpClient, urlGetLogin.getBaseClientRedirectUrl(), urlGetLogin.getPsuIpAddress());
        } catch (ProviderHttpStatusException | TokenInvalidException e) {
            throw new GetLoginInfoUrlFailedException("Failed to create consent step.", e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest request) {
        FabricGroupAuthenticationMeans authMeans = createAuthenticationMeans(request.getAuthenticationMeans(), providerIdentifier);
        FabricDefaultHttpClient defaultHttpHttpClient = createDefaultHttpHttpClient(authMeans, request.getRestTemplateManager());
        try {
            GroupProviderState providerState = getProviderState(request.getProviderState());
            return new AccessMeansOrStepDTO(authenticationService.createAccessMeans(
                    defaultHttpHttpClient,
                    providerState.getConsentId(),
                    providerState.getConsentValidTo(),
                    request.getUserId(),
                    request.getBaseClientRedirectUrl(),
                    request.getPsuIpAddress()));
        } catch (ProviderHttpStatusException | TokenInvalidException | JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Authorization step failed", e);
        }
    }

    @Override
    public void onUserSiteDelete(final UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        FabricGroupAuthenticationMeans authMeans = createAuthenticationMeans(urlOnUserSiteDeleteRequest.getAuthenticationMeans(), providerIdentifier);
        FabricDefaultHttpClient defaultHttpHttpClient = createDefaultHttpHttpClient(authMeans, urlOnUserSiteDeleteRequest.getRestTemplateManager());
        GroupProviderState providerState = getProviderState(urlOnUserSiteDeleteRequest.getAccessMeans().getAccessMeans());
        authenticationService.deleteConsent(defaultHttpHttpClient, providerState);
    }


    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws TokenInvalidException, ProviderFetchDataException {
        FabricGroupAuthenticationMeans authMeans = createAuthenticationMeans(urlFetchData.getAuthenticationMeans(), providerIdentifier);
        FabricDefaultHttpClient defaultHttpHttpClient = createDefaultHttpHttpClient(authMeans, urlFetchData.getRestTemplateManager());
        GroupProviderState providerState = getProviderState(urlFetchData.getAccessMeans().getAccessMeans());
        return fetchDataService.fetchData(
                providerState,
                defaultHttpHttpClient,
                urlFetchData.getPsuIpAddress(),
                urlFetchData.getTransactionsFetchStartTime());
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedClientConfiguration;
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        FabricGroupAuthenticationMeans clientConfiguration = createAuthenticationMeans(authMeans, providerIdentifier);
        FabricGroupOnboardingHttpClient httpClient = createHttpClient(urlAutoOnboardingRequest.getRestTemplateManager(), clientConfiguration, properties);
        httpClient.createRegistration(clientConfiguration, properties);

        return authMeans.entrySet().stream()
                .filter(entry -> !ONBOARDING_URL.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return new HashMap<>();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(signingKeyRequirements);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(transportKeyRequirements);
    }

    private GroupProviderState getProviderState(String serializedProviderState) throws TokenInvalidException {
        try {
            return objectMapper.readValue(serializedProviderState, GroupProviderState.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to deserialize provider access means from state");
        }
    }

    private FabricDefaultHttpClient createDefaultHttpHttpClient(final FabricGroupAuthenticationMeans authMeans, final RestTemplateManager restTemplateManager) {
        return restTemplateProducer.getMutualTlsClient(
                authMeans.getTransportKeyId(),
                authMeans.getClientTransportCertificate(),
                restTemplateManager);
    }
}
