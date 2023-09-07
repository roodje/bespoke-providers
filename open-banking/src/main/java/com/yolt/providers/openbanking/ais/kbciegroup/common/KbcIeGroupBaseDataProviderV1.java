package com.yolt.providers.openbanking.ais.kbciegroup.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.ConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.LoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.kbciegroup.common.service.autoonboarding.KbcIeGroupAutoOnboardingServiceV1;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class KbcIeGroupBaseDataProviderV1 extends GenericBaseDataProviderV2<LoginInfoState, AccessMeansState<AccessMeans>> implements AutoOnboardingProvider {

    private final KbcIeGroupAutoOnboardingServiceV1 autoOnboardingService;

    public KbcIeGroupBaseDataProviderV1(FetchDataServiceV2 fetchDataService,
                                        AccountRequestService accountRequestService,
                                        DefaultAuthenticationService authenticationService,
                                        HttpClientFactory httpClientFactory,
                                        TokenScope scope,
                                        ProviderIdentification providerIdentification,
                                        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                        Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                        AccessMeansStateMapper<AccessMeansState<AccessMeans>> accessMeansStateMapper,
                                        AccessMeansStateProvider<AccessMeansState<AccessMeans>> accessMeansStateProvider,
                                        Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                        Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                        Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                        LoginInfoStateMapper<LoginInfoState> loginInfoStateMapper,
                                        Function<List<String>, LoginInfoState> loginInfoStateProvider,
                                        ConsentPermissions consentPermissions,
                                        KbcIeGroupAutoOnboardingServiceV1 autoOnboardingService) {
        super(fetchDataService,
                accountRequestService,
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                getAuthenticationMeans,
                typedAuthenticationMeansSupplier,
                accessMeansStateMapper,
                accessMeansStateProvider,
                getSigningKeyRequirements,
                getTransportKeyRequirements,
                consentValidityRulesSupplier,
                loginInfoStateMapper,
                loginInfoStateProvider,
                consentPermissions);
        this.autoOnboardingService = autoOnboardingService;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return autoOnboardingService.getAutoConfiguredMeans();
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        return autoOnboardingService.register(urlAutoOnboardingRequest);
    }
}
