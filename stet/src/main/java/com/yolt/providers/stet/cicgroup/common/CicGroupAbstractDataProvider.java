package com.yolt.providers.stet.cicgroup.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.cicgroup.common.service.registration.CicGroupRegistrationService;
import com.yolt.providers.stet.generic.GenericOnboardingDataProvider;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;

public abstract class CicGroupAbstractDataProvider extends GenericOnboardingDataProvider {

    private final DefaultProperties properties;
    private final CicGroupRegistrationService registrationService;

    public CicGroupAbstractDataProvider(ExtendedAuthenticationMeansSupplier authMeansSupplier,
                                        HttpClientFactory httpClientFactory,
                                        CicGroupRegistrationService registrationService,
                                        AuthorizationService authorizationService,
                                        FetchDataService fetchDataService,
                                        ProviderStateMapper providerStateMapper,
                                        DefaultProperties properties,
                                        ConsentValidityRules consentValidityRules) {
        super(authMeansSupplier, httpClientFactory, registrationService, authorizationService, fetchDataService, providerStateMapper, properties, consentValidityRules);
        this.properties = properties;
        this.registrationService = registrationService;
    }

    @Override
    public void removeAutoConfiguration(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        DefaultAuthenticationMeans authMeans = createAuthMeans(urlAutoOnboardingRequest.getAuthenticationMeans());
        HttpClient httpClient = createHttpClient(urlAutoOnboardingRequest.getRestTemplateManager(), authMeans, properties.getRegistrationUrl());

        registrationService.deleteRegistration(httpClient, authMeans);
    }
}
