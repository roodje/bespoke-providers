package com.yolt.providers.openbanking.ais.revolutgroup.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
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
import com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutEuAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.autoonboarding.RevolutAutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.autoonboarding.RevolutGroupAutoOnboardingServiceV2;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RevolutGroupDataProviderV7 extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private final HttpClientFactory httpClientFactory;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final RevolutGroupAutoOnboardingServiceV2 autoOnboardingService;
    private final ProviderIdentification providerIdentification;
    private final String clintIdAuthMeanName;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAutoOnBoardingAuthMeans;

    public RevolutGroupDataProviderV7(FetchDataService fetchDataService, AccountRequestService accountRequestService, AuthenticationService authenticationService, HttpClientFactory httpClientFactory, TokenScope scope, ProviderIdentification providerIdentification, Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans, Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier, AccessMeansMapper accessMeansMapper, Supplier<Optional<KeyRequirements>> getSigningKeyRequirements, Supplier<Optional<KeyRequirements>> getTransportKeyRequirements, Supplier<ConsentValidityRules> consentValidityRulesSupplier, RevolutGroupAutoOnboardingServiceV2 autoOnboardingService, String clintIdAuthMeanName, Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAutoOnBoardingAuthMeans) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification, getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansMapper, getSigningKeyRequirements, getTransportKeyRequirements, consentValidityRulesSupplier);
        this.httpClientFactory = httpClientFactory;
        this.getAuthenticationMeans = getAuthenticationMeans;
        this.autoOnboardingService = autoOnboardingService;
        this.providerIdentification = providerIdentification;
        this.clintIdAuthMeanName = clintIdAuthMeanName;
        this.getAutoOnBoardingAuthMeans = getAutoOnBoardingAuthMeans;
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        throw new TokenInvalidException("Operation refresh access means is not supported by REVOLUT");
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        try {
            super.onUserSiteDelete(urlOnUserSiteDeleteRequest);
        } catch (HttpClientErrorException e) {
            if (!e.getStatusCode().equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                throw e;
            }
        }
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return getTypedAuthenticationMeans().entrySet()
                .stream()
                .filter(entry -> getAutoOnBoardingUnnecessaryMeans().contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected List<String> getAutoOnBoardingUnnecessaryMeans() {
        return List.of(RevolutEuAuthMeansBuilderV2.CLIENT_ID_NAME);
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        Map<String, BasicAuthenticationMean> mutableAuthMeans = new HashMap<>(authMeans);
        DefaultAuthMeans defaultAuthMeans = getAutoOnBoardingAuthMeans.apply(authMeans);

        RestTemplateManager manager = urlAutoOnboardingRequest.getRestTemplateManager();
        HttpClient httpClient = httpClientFactory.createHttpClient(manager, defaultAuthMeans, providerIdentification.getDisplayName());

        try {
            Optional<RevolutAutoOnboardingResponse> response = autoOnboardingService.register(httpClient, urlAutoOnboardingRequest, defaultAuthMeans);

            response.map(RevolutAutoOnboardingResponse::getClientId)
                    .map(clientId -> new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), clientId))
                    .ifPresent(authMean -> mutableAuthMeans.put(clintIdAuthMeanName, authMean));

        } catch (RestClientException | TokenInvalidException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Auto-onboarding failed for Revolut", e);
        }
        return mutableAuthMeans;
    }

    @Override
    public void removeAutoConfiguration(final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        DefaultAuthMeans defaultAuthMeans = getAuthenticationMeans.apply(authMeans);
        RestTemplateManager manager = urlAutoOnboardingRequest.getRestTemplateManager();
        HttpClient httpClient = httpClientFactory.createHttpClient(manager, defaultAuthMeans, providerIdentification.getDisplayName());
        try {
            autoOnboardingService.removeAutoConfiguration(httpClient, urlAutoOnboardingRequest, defaultAuthMeans);
        } catch (RestClientException | TokenInvalidException e) {
            throw new AutoOnboardingException(providerIdentification.getDisplayName(), "Removal of registration failed for Revolut", e);
        }
    }
}
