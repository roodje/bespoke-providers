package com.yolt.providers.openbanking.ais.monzogroup.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansMapper;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.MonzoGroupRegistrationServiceV2;

import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.monzogroup.common.auth.MonzoGroupAuthMeansMapper.CLIENT_ID_NAME;

public class MonzoGroupBaseDataProvider extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private final HttpClientFactory httpClientFactory;
    private final MonzoGroupRegistrationServiceV2 registrationService;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;

    public MonzoGroupBaseDataProvider(FetchDataService fetchDataService,
                                      AccountRequestService accountRequestService,
                                      AuthenticationService authenticationService,
                                      HttpClientFactory httpClientFactory,
                                      TokenScope scope,
                                      ProviderIdentification providerIdentification,
                                      Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                      Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                      AccessMeansMapper accessMeansMapper,
                                      Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                      Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                      Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                      MonzoGroupRegistrationServiceV2 registrationService) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification,
                getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansMapper, getSigningKeyRequirements,
                getTransportKeyRequirements, consentValidityRulesSupplier);
        this.httpClientFactory = httpClientFactory;
        this.registrationService = registrationService;
        this.getAuthenticationMeans = getAuthenticationMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return Collections.singletonMap(CLIENT_ID_NAME, CLIENT_ID_STRING);
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest request) {
        Map<String, BasicAuthenticationMean> authenticationMeans = request.getAuthenticationMeans();
        DefaultAuthMeans defaultAuthMeans = getAuthenticationMeans.apply(authenticationMeans);
        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), defaultAuthMeans, getProviderIdentifierDisplayName());
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);
        String registrationScope = OpenBankingTokenScope.getByTokenScopes(request.getScopes()).getRegistrationScope();

        try {
            registrationService.register(httpClient, request, registrationScope)
                    .ifPresent(clientRegistration -> {
                        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                clientRegistration.getClientId());
                        mutableMeans.put(CLIENT_ID_NAME, clientIdMean);
                    });
        } catch (TokenInvalidException | CertificateException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Dynamic registration failed.", e);
        }

        return mutableMeans;
    }
}
