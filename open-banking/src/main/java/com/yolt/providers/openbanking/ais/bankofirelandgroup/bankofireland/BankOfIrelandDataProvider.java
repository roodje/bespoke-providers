package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.BankOfIrelandGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.autoonboarding.BankOfIrelandGroupAutoOnboardingService;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.auth.BankOfIrelandAuthMeansMapper.CLIENT_ID_NAME_V2;


public class BankOfIrelandDataProvider extends BankOfIrelandGroupBaseDataProvider implements AutoOnboardingProvider {

    private final HttpClientFactory httpClientFactory;
    private final BankOfIrelandGroupAutoOnboardingService autoOnboardingService;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;

    public BankOfIrelandDataProvider(FetchDataService fetchDataService,
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
                                     BankOfIrelandGroupAutoOnboardingService autoOnboardingService) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification,
                getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansMapper, getSigningKeyRequirements,
                getTransportKeyRequirements, consentValidityRulesSupplier);
        this.httpClientFactory = httpClientFactory;
        this.autoOnboardingService = autoOnboardingService;
        this.getAuthenticationMeans = getAuthenticationMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = new HashMap<>();
        autoConfiguredMeans.put(CLIENT_ID_NAME_V2, CLIENT_ID_STRING);
        return autoConfiguredMeans;
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest request) {
        Map<String, BasicAuthenticationMean> authenticationMeans = request.getAuthenticationMeans();
        DefaultAuthMeans defaultAuthMeans = getAuthenticationMeans.apply(authenticationMeans);
        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), defaultAuthMeans, getProviderIdentifierDisplayName());
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);

        try {
            autoOnboardingService.register(httpClient, request)
                    .ifPresent(clientRegistration -> {
                        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                clientRegistration.getClientId());
                        mutableMeans.put(CLIENT_ID_NAME_V2, clientIdMean);
                    });
        } catch (TokenInvalidException | CertificateException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Dynamic registration failed.", e);
        }

        return mutableMeans;
    }
}
