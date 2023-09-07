package com.yolt.providers.openbanking.ais.tsbgroup.common;

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
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansMapper;
import com.yolt.providers.openbanking.ais.tsbgroup.common.auth.TsbGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.tsbgroup.common.service.TsbGroupRegistrationServiceV2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;

public class TsbGroupBaseDataProvider extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private final TsbGroupRegistrationServiceV2 registrationService;
    private final HttpClientFactory httpClientFactory;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final ProviderIdentification providerIdentification;

    public TsbGroupBaseDataProvider(FetchDataService fetchDataService,
                                    AccountRequestService accountRequestService,
                                    AuthenticationService authenticationService,
                                    HttpClientFactory httpClientFactory,
                                    TokenScope scope,
                                    ProviderIdentification providerIdentification,
                                    Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                    Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                    AccessMeansMapper accessMeansMapper, Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                    Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                    Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                    TsbGroupRegistrationServiceV2 registrationService) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification, getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansMapper, getSigningKeyRequirements, getTransportKeyRequirements, consentValidityRulesSupplier);
        this.httpClientFactory = httpClientFactory;
        this.getAuthenticationMeans = getAuthenticationMeans;
        this.registrationService = registrationService;
        this.providerIdentification = providerIdentification;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        Map<String, TypedAuthenticationMeans> authenticationMeans = new HashMap<>();
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.CLIENT_ID_NAME, CLIENT_ID_STRING);
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.CLIENT_SECRET_NAME, CLIENT_SECRET_STRING);
        return authenticationMeans;
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        DefaultAuthMeans defaultAuthMeans = getAuthenticationMeans.apply(authenticationMeans);
        HttpClient httpClient = httpClientFactory.createHttpClient(urlAutoOnboardingRequest.getRestTemplateManager(), defaultAuthMeans, providerIdentification.getIdentifier());
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);

        try {
            registrationService.register(httpClient, urlAutoOnboardingRequest)
                    .ifPresent(clientRegistration -> {
                        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                clientRegistration.getClientId());
                        BasicAuthenticationMean clientSecretMean = new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(),
                                clientRegistration.getClientSecret());
                        mutableMeans.put(TsbGroupAuthMeansBuilderV3.CLIENT_ID_NAME, clientIdMean);
                        mutableMeans.put(TsbGroupAuthMeansBuilderV3.CLIENT_SECRET_NAME, clientSecretMean);
                    });

        } catch (TokenInvalidException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Dynamic registration failed.", e);
        }

        return mutableMeans;
    }
}
