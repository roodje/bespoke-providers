package com.yolt.providers.openbanking.ais.vanquisgroup.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansMapper;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisAutoOnboardingServiceV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.auth.VanquisGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.http.VanquisGroupAutoOboardingRestTemplateFactory;
import com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.VanquisPropertiesV2;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;

public class VanquisGroupBaseDataProviderV2 extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private static final List<String> AUTO_ON_BOARDING_UNNECESSARY_MEANS = List.of(
            VanquisGroupAuthMeansBuilderV2.CLIENT_ID_NAME
    );
    private final VanquisPropertiesV2 properties;
    private final VanquisAutoOnboardingServiceV2 autoOnboardingService;

    public VanquisGroupBaseDataProviderV2(final FetchDataService fetchDataService,
                                          final AccountRequestService accountRequestService,
                                          final AuthenticationService authenticationService,
                                          final HttpClientFactory httpClientFactory,
                                          final TokenScope scope,
                                          final ProviderIdentification providerIdentification,
                                          final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansConverter,
                                          final Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                          final AccessMeansMapper accessMeansMapper,
                                          final Supplier<Optional<KeyRequirements>> signingKeyRequirementsSupplier,
                                          final Supplier<Optional<KeyRequirements>> transportKeyRequirementsSupplier,
                                          final Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                          final VanquisPropertiesV2 properties,
                                          final VanquisAutoOnboardingServiceV2 autoOnboardingService) {
        super(fetchDataService,
                accountRequestService,
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                authenticationMeansConverter,
                typedAuthenticationMeansSupplier,
                accessMeansMapper,
                signingKeyRequirementsSupplier,
                transportKeyRequirementsSupplier,
                consentValidityRulesSupplier);
        this.properties = properties;
        this.autoOnboardingService = autoOnboardingService;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return getTypedAuthenticationMeans().entrySet()
                .stream()
                .filter(entry -> AUTO_ON_BOARDING_UNNECESSARY_MEANS.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        RestTemplate restTemplate = VanquisGroupAutoOboardingRestTemplateFactory.createAutoOnBoardingRestTemplate(
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
                        mutableMeans.put(VanquisGroupAuthMeansBuilderV2.CLIENT_ID_NAME, clientIdMean);
                    });
        } catch (RestClientException | IllegalStateException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Auto-onboarding failed for Vanquis Bank", e);
        }

        return mutableMeans;
    }
}