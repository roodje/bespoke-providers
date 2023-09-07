package com.yolt.providers.openbanking.ais.tescobank;

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
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.ConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.LoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.tescobank.service.autoonboarding.TescoBankAutoOnboardingServiceV2;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.tescobank.auth.TescoBankAuthMeansBuilderV3.CLIENT_ID_NAME;


public class TescoBankBaseDataProviderV4 extends GenericBaseDataProviderV2 implements AutoOnboardingProvider {

    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansSupplier;
    private final HttpClientFactory httpClientFactory;
    private final TescoBankAutoOnboardingServiceV2 tescoBankAutoOnboardingServiceV2;

    public TescoBankBaseDataProviderV4(FetchDataServiceV2 fetchDataService,
                                       AccountRequestService accountRequestService,
                                       AuthenticationService authenticationService,
                                       HttpClientFactory httpClientFactory,
                                       TokenScope scope,
                                       ProviderIdentification providerIdentification,
                                       Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansSupplier,
                                       Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                       AccessMeansStateMapper<AccessMeansState> accessMeansStateMapper,
                                       AccessMeansStateProvider<AccessMeansState> accessMeansStateProvider,
                                       Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                       Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                       Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                       LoginInfoStateMapper<LoginInfoState> loginInfoStateMapper,
                                       Function<List<String>, LoginInfoState> loginInfoStateProvider,
                                       ConsentPermissions consentPermissions,
                                       TescoBankAutoOnboardingServiceV2 autoOnboardingServiceV2) {
        super(fetchDataService,
                accountRequestService,
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                authenticationMeansSupplier,
                typedAuthenticationMeansSupplier,
                accessMeansStateMapper,
                accessMeansStateProvider,
                getSigningKeyRequirements,
                getTransportKeyRequirements,
                consentValidityRulesSupplier,
                loginInfoStateMapper,
                loginInfoStateProvider,
                consentPermissions);
        this.authenticationMeansSupplier = authenticationMeansSupplier;
        this.httpClientFactory = httpClientFactory;
        this.tescoBankAutoOnboardingServiceV2 = autoOnboardingServiceV2;
    }


    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return Collections.singletonMap(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        DefaultAuthMeans authMeans = authenticationMeansSupplier.apply(authenticationMeans);

        HttpClient httpClient = httpClientFactory.createHttpClient(
                urlAutoOnboardingRequest.getRestTemplateManager(),
                authMeans,
                getProviderIdentifierDisplayName());
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);
        try {
            tescoBankAutoOnboardingServiceV2.register(httpClient, urlAutoOnboardingRequest)
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
