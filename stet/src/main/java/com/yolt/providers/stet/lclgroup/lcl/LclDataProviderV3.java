package com.yolt.providers.stet.lclgroup.lcl;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.GenericDataProvider;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;
import com.yolt.providers.stet.lclgroup.common.auth.LclGroupClientConfiguration;
import com.yolt.providers.stet.lclgroup.common.onboarding.LclGroupAutoOnBoardingService;
import com.yolt.providers.stet.lclgroup.common.onboarding.LclGroupClientRegistration;
import com.yolt.providers.stet.lclgroup.common.onboarding.LclRegistrationHttpClient;
import com.yolt.providers.stet.lclgroup.lcl.configuration.LclStetProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.stet.lclgroup.common.auth.LclGroupClientConfiguration.CLIENT_ID_NAME;

@Slf4j
public class LclDataProviderV3 extends GenericDataProvider implements AutoOnboardingProvider {

    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion version;
    private final LclStetProperties properties;
    private final LclGroupAutoOnBoardingService autoOnBoardingService;
    private final ExtendedAuthenticationMeansSupplier authMeansSupplier;

    public LclDataProviderV3(final ExtendedAuthenticationMeansSupplier authMeansSupplier,
                             final HttpClientFactory httpClientFactory,
                             final AuthorizationService authorizationService,
                             final FetchDataService fetchDataService,
                             final ProviderStateMapper providerStateMapper,
                             final LclGroupAutoOnBoardingService autoOnBoardingService,
                             final LclStetProperties properties,
                             final ConsentValidityRules consentValidityRules,
                             final String providerIdentifier,
                             final String providerIdentifierDisplayName,
                             final ProviderVersion version) {
        super(authMeansSupplier,
                httpClientFactory,
                authorizationService,
                fetchDataService,
                providerStateMapper,
                consentValidityRules);
        this.providerIdentifier = providerIdentifier;
        this.providerIdentifierDisplayName = providerIdentifierDisplayName;
        this.version = version;
        this.properties = properties;
        this.autoOnBoardingService = autoOnBoardingService;
        this.authMeansSupplier = authMeansSupplier;
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentifierDisplayName;
    }

    @Override
    public ProviderVersion getVersion() {
        return version;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return authMeansSupplier.getAutoConfiguredTypedAuthMeans();
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(final UrlAutoOnboardingRequest request) {
        Map<String, BasicAuthenticationMean> authMeans = request.getAuthenticationMeans();
        LclGroupClientConfiguration clientConfiguration = LclGroupClientConfiguration.fromAuthenticationMeansForAutoOnBoarding(authMeans, providerIdentifier);

        LclRegistrationHttpClient httpClient = LclRegistrationHttpClient.createHttpClient(request.getRestTemplateManager(), clientConfiguration, properties);

        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authMeans);
        try {
            String qSealCertificateUrl = clientConfiguration.getCertificateUrl(properties.getS3baseUrl(), getProviderIdentifier());
            SignatureData signatureData = new SignatureData(
                    request.getSigner(),
                    qSealCertificateUrl,
                    clientConfiguration.getClientSigningKeyId(),
                    clientConfiguration.getClientSigningCertificate(),
                    null,
                    properties.getRegistrationUrl()
            );

            if (authMeans.get(CLIENT_ID_NAME) != null && StringUtils.hasText(authMeans.get(CLIENT_ID_NAME).getValue())) {
                autoOnBoardingService.updateExistingRegistration(authMeans.get(CLIENT_ID_NAME).getValue(), httpClient, signatureData, clientConfiguration);
                return authMeans;
            } else {
                autoOnBoardingService
                        .register(request, clientConfiguration, httpClient, signatureData)
                        .ifPresent(clientRegistration -> updateConfigurationAfterRegistration(clientRegistration, mutableMeans));
            }
        } catch (RestClientException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Auto OnBoarding failed for LCL", e);
        }

        return mutableMeans;
    }

    private void updateConfigurationAfterRegistration(LclGroupClientRegistration clientRegistration,
                                                      Map<String, BasicAuthenticationMean> mutableMeans) {
        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), clientRegistration.getClientId());
        mutableMeans.put(CLIENT_ID_NAME, clientIdMean);
    }
}
