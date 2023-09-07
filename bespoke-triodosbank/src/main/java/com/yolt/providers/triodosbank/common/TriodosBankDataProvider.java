package com.yolt.providers.triodosbank.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans;
import com.yolt.providers.triodosbank.common.model.domain.TriodosBankProviderState;
import com.yolt.providers.triodosbank.common.model.http.ConsentCreationResponse;
import com.yolt.providers.triodosbank.common.model.http.TokenResponse;
import com.yolt.providers.triodosbank.common.rest.TriodosBankHttpClient;
import com.yolt.providers.triodosbank.common.rest.TriodosBankHttpClientFactory;
import com.yolt.providers.triodosbank.common.service.TriodosBankAuthorizationService;
import com.yolt.providers.triodosbank.common.service.TriodosBankFetchDataService;
import com.yolt.providers.triodosbank.common.service.TriodosBankRegistrationService;
import com.yolt.providers.triodosbank.common.util.HsmUtils;
import com.yolt.providers.triodosbank.common.util.TriodosBankPKCE;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans.*;

@Service
@Slf4j
public abstract class TriodosBankDataProvider implements UrlDataProvider, AutoOnboardingProvider {

    private final TriodosBankRegistrationService registrationService;
    private final TriodosBankAuthorizationService authorizationService;
    private final TriodosBankFetchDataService fetchDataService;
    private final TriodosBankHttpClientFactory httpClientFactory;
    private final ObjectMapper mapper;
    private final Clock clock;

    public TriodosBankDataProvider(TriodosBankRegistrationService registrationService, TriodosBankAuthorizationService authorizationService,
                                   TriodosBankFetchDataService fetchDataService, TriodosBankHttpClientFactory httpClientFactory,
                                   @Qualifier("TriodosBankObjectMapper") ObjectMapper mapper, final Clock clock) {
        this.registrationService = registrationService;
        this.authorizationService = authorizationService;
        this.fetchDataService = fetchDataService;
        this.httpClientFactory = httpClientFactory;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return TriodosBankAuthenticationMeans.getAutoConfiguredMeans();
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest request) {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>(request.getAuthenticationMeans());

        TriodosBankAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        TriodosBankHttpClient httpClient = httpClientFactory.createHttpClient(authMeans, request.getRestTemplateManager(), request.getSigner());
        Map<String, BasicAuthenticationMean> registeredAuthMeans = registrationService.register(
                httpClient,
                request.getRedirectUrls(),
                getProviderIdentifier());
        authenticationMeans.putAll(registeredAuthMeans);
        return authenticationMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return TriodosBankAuthenticationMeans.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest request) {
        TriodosBankAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        OAuth2ProofKeyCodeExchange codeExchange = TriodosBankPKCE.createRandomS256();

        TriodosBankHttpClient httpClient = httpClientFactory.createHttpClient(authMeans, request.getRestTemplateManager(), request.getSigner());

        ConsentCreationResponse consentResponse = authorizationService.initiateConsentAndUpdateScaRedirectUrl(
                httpClient, request.getBaseClientRedirectUrl(), request.getPsuIpAddress(), codeExchange);

        String providerState = serialize(new TriodosBankProviderState(codeExchange, consentResponse));
        return new RedirectStep(consentResponse.getLinks().getScaRedirect() + "&state=" + request.getState(), request.getExternalConsentId(), providerState);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest request) {
        TriodosBankAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        TriodosBankHttpClient httpClient = httpClientFactory.createHttpClient(authMeans, request.getRestTemplateManager(), request.getSigner());
        try {
            TriodosBankProviderState providerState = deserialize(request.getProviderState());

            String consentId = providerState.getConsentId();
            authorizationService.validateConsentStatus(httpClient, consentId);

            TokenResponse token = authorizationService.getAccessToken(httpClient, request.getRedirectUrlPostedBackFromSite(), providerState.getCodeVerifier());
            providerState.setTokens(token);

            authorizationService.authoriseConsent(httpClient, consentId, providerState.getAuthorisationId(), request.getPsuIpAddress(), token.getAccessToken());
            final Date expirationDate = Date.from(Instant.now(clock).plusSeconds(token.getExpiresIn()));
            return new AccessMeansOrStepDTO(createAccessMeansDTO(request.getUserId(), providerState, expirationDate));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        TriodosBankAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        AccessMeansDTO accessMeansDTO = request.getAccessMeans();
        TriodosBankProviderState providerState = deserialize(accessMeansDTO.getAccessMeans());

        TriodosBankHttpClient httpClient = httpClientFactory.createHttpClient(authMeans, request.getRestTemplateManager(), request.getSigner());
        TokenResponse token = authorizationService.refreshAccessToken(httpClient, providerState.getRefreshToken());
        providerState.setTokens(token);
        final Date expirationDate = Date.from(Instant.now(clock).plusSeconds(token.getExpiresIn()));
        return createAccessMeansDTO(accessMeansDTO.getUserId(), providerState, expirationDate);
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        TriodosBankAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        TriodosBankHttpClient httpClient = httpClientFactory.createHttpClient(authMeans, request.getRestTemplateManager(), request.getSigner());

        TriodosBankProviderState providerState = deserialize(request.getAccessMeans().getAccessMeans());
        return fetchDataService.fetchData(httpClient, request.getTransactionsFetchStartTime(), providerState);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private TriodosBankAuthenticationMeans createAuthMeans(Map<String, BasicAuthenticationMean> authenticationMeans) {
        return createAuthenticationMeans(authenticationMeans, getProviderIdentifier());
    }

    private AccessMeansDTO createAccessMeansDTO(UUID userId, TriodosBankProviderState providerState, Date expirationDate) {
        return new AccessMeansDTO(userId, serialize(providerState), new Date(), expirationDate);
    }

    private String serialize(final TriodosBankProviderState providerState) {
        try {
            return mapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }

    private TriodosBankProviderState deserialize(final String providerState) throws TokenInvalidException {
        try {
            return mapper.readValue(providerState, TriodosBankProviderState.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize provider state");
        }
    }
}
