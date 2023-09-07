package com.yolt.providers.stet.generic;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;
import com.yolt.providers.stet.generic.service.registration.RegistrationService;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class GenericOnboardingDataProvider extends GenericDataProvider implements AutoOnboardingProvider {

    private final ExtendedAuthenticationMeansSupplier authMeansSupplier;
    private final RegistrationService registrationService;
    private final DefaultProperties properties;

    public GenericOnboardingDataProvider(ExtendedAuthenticationMeansSupplier authMeansSupplier,
                                         HttpClientFactory httpClientFactory,
                                         RegistrationService registrationService,
                                         AuthorizationService authorizationService,
                                         FetchDataService fetchDataService,
                                         ProviderStateMapper providerStateMapper,
                                         DefaultProperties properties,
                                         ConsentValidityRules consentValidityRules) {
        super(authMeansSupplier, httpClientFactory, authorizationService, fetchDataService, providerStateMapper, consentValidityRules);
        this.authMeansSupplier = authMeansSupplier;
        this.registrationService = registrationService;
        this.properties = properties;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return authMeansSupplier.getAutoConfiguredTypedAuthMeans();
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest request) {
        DefaultAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        HttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authMeans, properties.getRegistrationUrl());

        RegistrationRequest registrationRequest = new RegistrationRequest(
                authMeans,
                request.getSigner(),
                () -> UUID.randomUUID().toString(),
                request.getBaseClientRedirectUrl(),
                getProviderIdentifier());

        Map<String, BasicAuthenticationMean> register = registrationService.register(httpClient, registrationRequest);
        Map<String, BasicAuthenticationMean> enhancedAuthMeans = new HashMap<>(request.getAuthenticationMeans());
        enhancedAuthMeans.putAll(register);
        return enhancedAuthMeans;
    }
}
