package com.yolt.providers.openbanking.ais.tidegroup.common;

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
import com.yolt.providers.openbanking.ais.tidegroup.common.service.TideGroupAutoOnboardingServiceV2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.tidegroup.common.auth.TideGroupAuthMeansMapperV3.CLIENT_ID_NAME;

public class TideGroupDataProviderV2 extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private final HttpClientFactory httpClientFactory;
    private final TideGroupAutoOnboardingServiceV2 autoOnboardingService;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansMapper;

    public TideGroupDataProviderV2(FetchDataService fetchDataService,
                                   AccountRequestService accountRequestService,
                                   AuthenticationService authenticationService,
                                   HttpClientFactory httpClientFactory,
                                   TokenScope scope,
                                   ProviderIdentification providerIdentification,
                                   Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansMapper,
                                   Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                   AccessMeansMapper accessMeansMapper,
                                   Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                   Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                   Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                   TideGroupAutoOnboardingServiceV2 autoOnboardingService) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification,
                authenticationMeansMapper, typedAuthenticationMeansSupplier, accessMeansMapper, getSigningKeyRequirements,
                getTransportKeyRequirements, consentValidityRulesSupplier);
        this.httpClientFactory = httpClientFactory;
        this.autoOnboardingService = autoOnboardingService;
        this.authenticationMeansMapper = authenticationMeansMapper;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = new HashMap<>();
        autoConfiguredMeans.put(CLIENT_ID_NAME, CLIENT_ID_STRING);
        return autoConfiguredMeans;
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest request) {
        Map<String, BasicAuthenticationMean> authenticationMeans = request.getAuthenticationMeans();
        DefaultAuthMeans defaultAuthMeans = authenticationMeansMapper.apply(authenticationMeans);
        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), defaultAuthMeans, getProviderIdentifierDisplayName());
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);

        try {
            autoOnboardingService.register(httpClient, request)
                    .ifPresent(clientRegistration -> {
                        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                clientRegistration.getClientId());
                        mutableMeans.put(CLIENT_ID_NAME, clientIdMean);
                    });
        } catch (TokenInvalidException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Dynamic registration failed.", e);
        }

        return mutableMeans;
    }
}
