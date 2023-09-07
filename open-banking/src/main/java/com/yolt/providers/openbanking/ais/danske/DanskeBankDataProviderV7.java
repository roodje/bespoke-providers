package com.yolt.providers.openbanking.ais.danske;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.danske.service.autoonboarding.DanskeBankAutoOnboardingServiceV2;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansMapper;

import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.danske.oauth2.DanskeAuthMeansBuilderV3.CLIENT_ID_NAME;

public class DanskeBankDataProviderV7 extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private final DanskeBankAutoOnboardingServiceV2 autoOnboardingService;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final HttpClientFactory httpClientFactory;

    public DanskeBankDataProviderV7(FetchDataService fetchDataService,
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
                                    DanskeBankAutoOnboardingServiceV2 autoOnboardingService) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification, getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansMapper, getSigningKeyRequirements, getTransportKeyRequirements, consentValidityRulesSupplier);
        this.autoOnboardingService = autoOnboardingService;
        this.getAuthenticationMeans = getAuthenticationMeans;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return Collections.singletonMap(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        DefaultAuthMeans defaultAuthMeans = getAuthenticationMeans.apply(authenticationMeans);
        HttpClient httpClient = httpClientFactory.createHttpClient(urlAutoOnboardingRequest.getRestTemplateManager(), defaultAuthMeans, getProviderIdentifierDisplayName());
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);
        try {
            autoOnboardingService.register(httpClient, urlAutoOnboardingRequest)
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