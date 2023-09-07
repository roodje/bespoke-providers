package com.yolt.providers.argentagroup.common;

import com.yolt.providers.argentagroup.common.exception.MalformedObjectException;
import com.yolt.providers.argentagroup.common.http.DefaultHttpClientFactory;
import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.consent.ConsentService;
import com.yolt.providers.argentagroup.common.service.fetchdata.FetchDataService;
import com.yolt.providers.argentagroup.common.service.token.AuthorizationService;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class ArgentaGroupDataProvider implements UrlDataProvider {

    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion version;
    private final Map<String, TypedAuthenticationMeans> typedAuthenticationMeans;
    private final ConsentValidityRules consentValidityRules;
    private final BiFunction<String, Map<String, BasicAuthenticationMean>, DefaultAuthenticationMeans> authenticationMeansMapperFunction;
    private final DefaultHttpClientFactory httpClientFactory;
    private final AuthorizationService authorizationService;
    private final ConsentService consentService;
    private final FetchDataService fetchDataService;


    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        DefaultAuthenticationMeans authenticationMeans = authenticationMeansMapperFunction.apply(providerIdentifier, request.getAuthenticationMeans());

        HttpClient httpClient = httpClientFactory.createArgentaGroupHttpClient(
                authenticationMeans,
                request.getRestTemplateManager(),
                providerIdentifier
        );

        return fetchDataService.fetchData(request, authenticationMeans, httpClient);
    }


    @Override
    public Step getLoginInfo(final UrlGetLoginRequest request) {
        DefaultAuthenticationMeans authenticationMeans = authenticationMeansMapperFunction.apply(providerIdentifier, request.getAuthenticationMeans());

        HttpClient httpClient = httpClientFactory.createArgentaGroupHttpClient(
                authenticationMeans,
                request.getRestTemplateManager(),
                providerIdentifier
        );
        try {
            return consentService.generateAuthorizationUrlStep(request, authenticationMeans, httpClient);
        } catch (TokenInvalidException | MalformedObjectException e) {
            throw new GetLoginInfoUrlFailedException("Get login info failed", e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest request) {
        DefaultAuthenticationMeans authenticationMeans = authenticationMeansMapperFunction.apply(providerIdentifier, request.getAuthenticationMeans());

        HttpClient httpClient = httpClientFactory.createArgentaGroupHttpClient(
                authenticationMeans,
                request.getRestTemplateManager(),
                providerIdentifier
        );
        try {
            return authorizationService.createNewAccessMeans(request, authenticationMeans, httpClient);
        } catch (TokenInvalidException | MalformedObjectException e) {
            throw new GetAccessTokenFailedException(e.getMessage(), e);
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        DefaultAuthenticationMeans authenticationMeans = authenticationMeansMapperFunction.apply(providerIdentifier, request.getAuthenticationMeans());

        HttpClient httpClient = httpClientFactory.createArgentaGroupHttpClient(
                authenticationMeans,
                request.getRestTemplateManager(),
                providerIdentifier
        );

        return authorizationService.refreshAccessMeans(request, authenticationMeans, httpClient);
    }

    @Override
    public void onUserSiteDelete(final UrlOnUserSiteDeleteRequest request) throws TokenInvalidException {
        DefaultAuthenticationMeans authenticationMeans = authenticationMeansMapperFunction.apply(providerIdentifier, request.getAuthenticationMeans());

        HttpClient httpClient = httpClientFactory.createArgentaGroupHttpClient(
                authenticationMeans,
                request.getRestTemplateManager(),
                providerIdentifier
        );

        consentService.deleteUserConsent(request, authenticationMeans, httpClient);
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
        return version;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedAuthenticationMeans;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return consentValidityRules;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(HsmEIdasUtils.getKeyRequirements(
                DefaultAuthenticationMeans.SIGNING_KEY_ID_NAME, DefaultAuthenticationMeans.SIGNING_CERTIFICATE_NAME
        ));
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(HsmEIdasUtils.getKeyRequirements(
                DefaultAuthenticationMeans.TRANSPORT_KEY_ID_NAME, DefaultAuthenticationMeans.TRANSPORT_CERTIFICATE_NAME
        ));
    }
}
