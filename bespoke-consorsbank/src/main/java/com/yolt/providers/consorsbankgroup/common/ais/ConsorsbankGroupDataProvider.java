package com.yolt.providers.consorsbankgroup.common.ais;

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
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.consorsbankgroup.common.ais.http.HttpClientFactory;
import com.yolt.providers.consorsbankgroup.common.ais.service.DefaultAuthenticationMeans;
import com.yolt.providers.consorsbankgroup.common.ais.service.DefaultAuthorizationService;
import com.yolt.providers.consorsbankgroup.common.ais.service.DefaultFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class ConsorsbankGroupDataProvider implements UrlDataProvider {

    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion providerVersion;
    private final Clock clock;
    private final Map<String, TypedAuthenticationMeans> typedAuthenticationMeans;
    private final KeyRequirements transportKeyRequirements;
    private final AccessMeansMapper accessMeansMapper;
    private final HttpClientFactory httpClientFactory;
    private final DefaultAuthorizationService authorizationService;
    private final DefaultFetchDataService fetchDataService;

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        DefaultAuthenticationMeans authenticationMeans = DefaultAuthenticationMeans
                .fromAuthMeans(providerIdentifier, request.getAuthenticationMeans());

        HttpClient httpClient = httpClientFactory
                .createHttpClient(authenticationMeans, request.getRestTemplateManager(), providerIdentifier);

        String serializedAccessMeans = request.getAccessMeans().getAccessMeans();
        DefaultAccessMeans accessMeans = accessMeansMapper.readAccessMeans(serializedAccessMeans);

        return fetchDataService.fetchData(accessMeans,
                LocalDate.ofInstant(request.getTransactionsFetchStartTime(), ZoneId.of("UTC")),
                httpClient,
                request.getPsuIpAddress());
    }

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest request) {
        DefaultAuthenticationMeans authenticationMeans = DefaultAuthenticationMeans
                .fromAuthMeans(providerIdentifier, request.getAuthenticationMeans());

        HttpClient httpClient = httpClientFactory
                .createHttpClient(authenticationMeans, request.getRestTemplateManager(), providerIdentifier);

        return authorizationService.getLoginInfo(request.getBaseClientRedirectUrl(),
                request.getPsuIpAddress(),
                request.getState(),
                httpClient);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest request) {
        DefaultAccessMeans accessMeans = new DefaultAccessMeans(request.getProviderState());

        String serializedAccessMeans = accessMeansMapper.serializeAccessMeans(accessMeans);
        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        request.getUserId(),
                        serializedAccessMeans,
                        new Date(),
                        Date.from(Instant.now(clock).plus(89, ChronoUnit.DAYS))
                )
        );
    }

    @Override
    public void onUserSiteDelete(final UrlOnUserSiteDeleteRequest request) throws TokenInvalidException {
        DefaultAuthenticationMeans authenticationMeans = DefaultAuthenticationMeans
                .fromAuthMeans(providerIdentifier, request.getAuthenticationMeans());

        HttpClient httpClient = httpClientFactory
                .createHttpClient(authenticationMeans, request.getRestTemplateManager(), providerIdentifier);

        String serializedAccessMeans = request.getAccessMeans().getAccessMeans();
        DefaultAccessMeans accessMeans = accessMeansMapper.readAccessMeans(serializedAccessMeans);

        authorizationService.deleteConsent(accessMeans, request.getPsuIpAddress(), httpClient);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        throw new TokenInvalidException(String.format("Operation refresh access means is not supported by %s", providerIdentifier));
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(transportKeyRequirements);
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
        return typedAuthenticationMeans;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
