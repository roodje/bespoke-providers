package com.yolt.providers.openbanking.ais.hsbcgroup.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.ConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.LoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.properties.HsbcGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.HsbcGroupAutoOnboardingServiceV3;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.utils.HsbcGroupAutoOnboardingUtilsV2;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3.CLIENT_ID_NAME;

public class HsbcGroupBaseDataProviderV7 extends GenericBaseDataProviderV2 implements AutoOnboardingProvider {

    private final HsbcGroupPropertiesV2 properties;
    private final HsbcGroupAutoOnboardingServiceV3 autoOnboardingService;

    public HsbcGroupBaseDataProviderV7(FetchDataServiceV2 fetchDataService,
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
                                       Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                       LoginInfoStateMapper loginInfoStateMapper,
                                       Function loginInfoStateProvider,
                                       ConsentPermissions consentPermissions,
                                       HsbcGroupPropertiesV2 properties,
                                       HsbcGroupAutoOnboardingServiceV3 autoOnboardingService) {
        super(fetchDataService, accountRequestService, authenticationService,
                httpClientFactory, scope, providerIdentification,
                getAuthenticationMeans, typedAuthenticationMeansSupplier,
                accessMeansStateMapper, accessMeansStateProvider, getSigningKeyRequirements, getTransportKeyRequirements, consentValidityRulesSupplier, loginInfoStateMapper, loginInfoStateProvider, consentPermissions);
        this.properties = properties;
        this.autoOnboardingService = autoOnboardingService;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = new HashMap<>();
        autoConfiguredMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        return autoConfiguredMeans;
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        RestTemplate restTemplate = HsbcGroupAutoOnboardingUtilsV2.createAutoOnBoardingRestTemplate(
                urlAutoOnboardingRequest.getRestTemplateManager(),
                authenticationMeans,
                properties,
                getProviderIdentifier());

        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);

        try {
            autoOnboardingService
                    .register(restTemplate, urlAutoOnboardingRequest, getProviderIdentifier(), properties)
                    .ifPresent(clientRegistration -> {
                        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                clientRegistration.getClientId());
                        mutableMeans.put(CLIENT_ID_NAME, clientIdMean);
                    });
        } catch (RestClientException | IllegalStateException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Auto-onboarding failed for " + getProviderIdentifierDisplayName(), e);
        }

        return mutableMeans;
    }
}
