package com.yolt.providers.openbanking.ais.newdaygroup.amazoncreditcard;

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
import com.yolt.providers.openbanking.ais.newdaygroup.common.auth.NewDayGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.newdaygroup.common.http.NewDayGroupOnboardingRestTemplateFactory;
import com.yolt.providers.openbanking.ais.newdaygroup.common.service.NewDayGroupAutoOnboardingServiceV2;
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
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;

public class AmazonCreditCardDataProviderV3 extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private static final List<String> AUTO_ON_BOARDING_UNNECESSARY_MEANS = List.of(
            NewDayGroupAuthMeansBuilderV2.CLIENT_ID_NAME,
            NewDayGroupAuthMeansBuilderV2.CLIENT_SECRET_NAME
    );
    private final AmazonCreditCardPropertiesV2 properties;
    private final NewDayGroupAutoOnboardingServiceV2 autoOnboardingService;

    public AmazonCreditCardDataProviderV3(final FetchDataService fetchDataService,
                                          final AccountRequestService accountRequestService,
                                          final AuthenticationService authenticationService,
                                          final HttpClientFactory httpClientFactory,
                                          final TokenScope scope,
                                          final ProviderIdentification providerIdentification,
                                          final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                          final Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                          final AccessMeansMapper accessMeansMapper,
                                          final Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                          final Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                          final Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                          final AmazonCreditCardPropertiesV2 properties,
                                          final NewDayGroupAutoOnboardingServiceV2 autoOnboardingService) {
        super(fetchDataService,
                accountRequestService,
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                getAuthenticationMeans,
                typedAuthenticationMeansSupplier,
                accessMeansMapper,
                getSigningKeyRequirements,
                getTransportKeyRequirements,
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
        RestTemplate autoOnBoardingRestTemplate = NewDayGroupOnboardingRestTemplateFactory.createAutoOnBoardingRestTemplate(
                urlAutoOnboardingRequest.getRestTemplateManager(),
                authenticationMeans,
                properties,
                getProviderIdentifier()
        );
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);

        try {
            autoOnboardingService
                    .register(autoOnBoardingRestTemplate, urlAutoOnboardingRequest, getProviderIdentifier())
                    .ifPresent(clientRegistration -> {
                        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                clientRegistration.getClientId());
                        BasicAuthenticationMean clientSecretMean = new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(),
                                clientRegistration.getClientSecret());
                        mutableMeans.put(NewDayGroupAuthMeansBuilderV2.CLIENT_ID_NAME, clientIdMean);
                        mutableMeans.put(NewDayGroupAuthMeansBuilderV2.CLIENT_SECRET_NAME, clientSecretMean);
                    });
        } catch (RestClientException | IllegalStateException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Auto-onboarding failed for Amazon Credit Card", e);
        }

        return mutableMeans;
    }

}