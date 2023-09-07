package com.yolt.providers.openbanking.ais.permanenttsbgroup.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.ConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.LoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.service.PermanentTsbGroupAutoOnboardingServiceV1;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class PermanentTsbGroupBaseDataProviderV1 extends GenericBaseDataProviderV2<LoginInfoState, AccessMeansState<AccessMeans>> implements AutoOnboardingProvider {

    private final PermanentTsbGroupAutoOnboardingServiceV1 autoOnboardingService;

    public PermanentTsbGroupBaseDataProviderV1(FetchDataServiceV2 fetchDataService,
                                               AccountRequestService accountRequestService,
                                               AuthenticationService authenticationService,
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
                                               PermanentTsbGroupAutoOnboardingServiceV1 autoOnboardingService) {
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
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        throw new TokenInvalidException("Permanent TSB Group does not support refresh token. An access token is simply valid for 90 days. If expired, the user needs to relogin");
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return autoOnboardingService.getAutoConfiguredMeans(getTypedAuthenticationMeans());
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        return autoOnboardingService.register(urlAutoOnboardingRequest);
    }
}
