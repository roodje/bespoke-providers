package com.yolt.providers.openbanking.ais.sainsburys;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
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
import com.yolt.providers.openbanking.ais.sainsburys.service.SainsburysAutoOnboardingServiceV2;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.sainsburys.auth.SainsburysAuthMeansMapperV2.CLIENT_ID_NAME;

public class SainsburysBaseDataProvider extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private final HttpClientFactory httpClientFactory;
    private final SainsburysAutoOnboardingServiceV2 autoOnboardingService;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;

    public SainsburysBaseDataProvider(FetchDataService fetchDataService,
                                      AccountRequestService accountRequestService,
                                      AuthenticationService authenticationService,
                                      HttpClientFactory httpClientFactory,
                                      TokenScope scope,
                                      ProviderIdentification providerIdentification,
                                      Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                      Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                      AccessMeansMapper accessMeansMapper,
                                      Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                      Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                      Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                      SainsburysAutoOnboardingServiceV2 autoOnboardingService) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification,
                getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansMapper, getSigningKeyRequirements,
                getTransportKeyRequirements, consentValidityRulesSupplier);
        this.httpClientFactory = httpClientFactory;
        this.autoOnboardingService = autoOnboardingService;
        this.getAuthenticationMeans = getAuthenticationMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return Collections.singletonMap(CLIENT_ID_NAME, CLIENT_ID_STRING);
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
                        mutableMeans.put(CLIENT_ID_NAME, clientIdMean);
                    });
        } catch (HttpClientErrorException | TokenInvalidException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Dynamic registration failed.", e);
        }

        return mutableMeans;
    }

    @Override
    public void removeAutoConfiguration(UrlAutoOnboardingRequest request) {
        Map<String, BasicAuthenticationMean> authenticationMeans = request.getAuthenticationMeans();
        DefaultAuthMeans authMeans = getAuthenticationMeans.apply(authenticationMeans);
        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authMeans, getProviderIdentifierDisplayName());
        try {
            autoOnboardingService.removeRegistration(httpClient, authMeans, request.getSigner());
        } catch (TokenInvalidException e) {
            throw new ProviderHttpStatusException("Unable to remove client registration for " + getProviderIdentifierDisplayName(), e);
        }
    }
}
