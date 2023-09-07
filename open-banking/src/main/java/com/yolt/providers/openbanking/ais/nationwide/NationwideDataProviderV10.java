package com.yolt.providers.openbanking.ais.nationwide;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.ConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultLoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.nationwide.service.autoonboarding.NationwideAutoOnboardingServiceV3;

import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3.CLIENT_ID_NAME;

public class NationwideDataProviderV10 extends GenericBaseDataProviderV2 implements AutoOnboardingProvider {


    private final NationwideAutoOnboardingServiceV3 autoOnboardingService;
    private final HttpClientFactory httpClientFactory;
    private final ProviderIdentification providerIdentification;
    private final TokenScope tokenScope;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;


    public NationwideDataProviderV10(FetchDataServiceV2 fetchDataService,
                                     AccountRequestService accountRequestService,
                                     AuthenticationService authenticationService,
                                     HttpClientFactory httpClientFactory,
                                     TokenScope scope,
                                     ProviderIdentification providerIdentification,
                                     Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                     Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                     AccessMeansStateMapper accessMeansStateMapper,
                                     AccessMeansStateProvider accessMeansStateProvider,
                                     Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                     Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                     NationwideAutoOnboardingServiceV3 autoOnboardingService,
                                     Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                     DefaultLoginInfoStateMapper loginInfoStateMapper,
                                     Function loginInfoStateProvider,
                                     ConsentPermissions consentPermissions) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification, getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansStateMapper, accessMeansStateProvider, getSigningKeyRequirements, getTransportKeyRequirements, consentValidityRulesSupplier, loginInfoStateMapper, loginInfoStateProvider, consentPermissions);
        this.autoOnboardingService = autoOnboardingService;
        this.httpClientFactory = httpClientFactory;
        this.getAuthenticationMeans = getAuthenticationMeans;
        this.providerIdentification = providerIdentification;
        this.tokenScope = scope;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        return typedAuthenticationMeans;
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        DefaultAuthMeans defaultAuthMeans = getAuthenticationMeans.apply(authenticationMeans);
        HttpClient httpClient = httpClientFactory.createHttpClient(urlAutoOnboardingRequest.getRestTemplateManager(), defaultAuthMeans, providerIdentification.getIdentifier());
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);

        try {
            autoOnboardingService.register(httpClient, defaultAuthMeans, tokenScope, urlAutoOnboardingRequest)
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