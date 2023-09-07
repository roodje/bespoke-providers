package com.yolt.providers.openbanking.ais.cybgroup.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupAutoOnboardingServiceV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.auth.CybgGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.config.CybgGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.http.CybgAutoOnboardingRestTemplateFactory;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupClientRegistration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansMapper;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;

public class CybgGroupDataProviderV3 extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private static final List<String> AUTO_ON_BOARDING_UNNECESSARY_MEANS = List.of(
            CybgGroupAuthMeansBuilderV2.CLIENT_ID_NAME,
            CybgGroupAuthMeansBuilderV2.CLIENT_SECRET_NAME,
            CybgGroupAuthMeansBuilderV2.INSTITUTION_ID_NAME);

    private final CybgGroupPropertiesV2 properties;
    private final CybgGroupAutoOnboardingServiceV2 cybgGroupAutoOnboardingService;
    private final DefaultAccessMeansMapper<CybgGroupAccessMeansV2> accessMeansMapper;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansFactoryFunction;
    private final HttpClientFactory httpClientFactory;
    private final AuthenticationService authenticationService;
    private final TokenScope scope;

    public CybgGroupDataProviderV3(CybgGroupPropertiesV2 properties,
                                   FetchDataService fetchDataService,
                                   AccountRequestService accountRequestService,
                                   AuthenticationService authenticationService,
                                   HttpClientFactory httpClientFactory,
                                   TokenScope scope,
                                   ProviderIdentification providerIdentification,
                                   Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansFactoryFunction,
                                   Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                   DefaultAccessMeansMapper<CybgGroupAccessMeansV2> accessMeansMapper,
                                   Supplier<Optional<KeyRequirements>> signingKeyRequirementsSupplier,
                                   Supplier<Optional<KeyRequirements>> transportKeyRequirementsSupplier,
                                   CybgGroupAutoOnboardingServiceV2 autoOnboardingService,
                                   Supplier<ConsentValidityRules> consentValidityRulesSupplier) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification,
                authenticationMeansFactoryFunction, typedAuthenticationMeansSupplier, accessMeansMapper, signingKeyRequirementsSupplier, transportKeyRequirementsSupplier, consentValidityRulesSupplier);
        this.cybgGroupAutoOnboardingService = autoOnboardingService;
        this.properties = properties;
        this.accessMeansMapper = accessMeansMapper;
        this.authenticationMeansFactoryFunction = authenticationMeansFactoryFunction;
        this.httpClientFactory = httpClientFactory;
        this.authenticationService = authenticationService;
        this.scope = scope;

    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        AccessMeansDTO accessMeans = request.getAccessMeans();
        CybgGroupAccessMeansV2 oAuthToken = accessMeansMapper.fromJson(accessMeans.getAccessMeans());
        DefaultAuthMeans authenticationMeans = authenticationMeansFactoryFunction.apply((request.getAuthenticationMeans()));

        if (StringUtils.isEmpty(oAuthToken.getRefreshToken())) {
            throw new TokenInvalidException("Refresh token is missing, and access token is expired.");
        }

        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

        AccessMeans newOAuthToken = authenticationService.refreshAccessToken(httpClient, authenticationMeans,
                oAuthToken.getUserId(), oAuthToken.getRefreshToken(), oAuthToken.getRedirectUri(), scope,
                request.getSigner());
        String newOAuthTokenJSON = accessMeansMapper.toJson(new CybgGroupAccessMeansV2(newOAuthToken, oAuthToken.getCachedAccounts()));
        return new AccessMeansDTO(
                accessMeans.getUserId(),
                newOAuthTokenJSON,
                new Date(),
                newOAuthToken.getExpireTime()
        );
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return getTypedAuthenticationMeans().entrySet()
                .stream()
                .filter(entry -> AUTO_ON_BOARDING_UNNECESSARY_MEANS.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        RestTemplate restTemplate = CybgAutoOnboardingRestTemplateFactory.createAutoOnBoardingRestTemplate(
                urlAutoOnboardingRequest.getRestTemplateManager(),
                authenticationMeans,
                properties,
                getProviderIdentifier());

        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);

        try {
            cybgGroupAutoOnboardingService
                    .register(restTemplate, urlAutoOnboardingRequest, getProviderIdentifier(), properties)
                    .ifPresent(clientRegistration -> extractDataAndUpdateMeans(clientRegistration, mutableMeans));
        } catch (RestClientException | IllegalStateException e) {
            throw new AutoOnboardingException(getProviderIdentifierDisplayName(), "Auto-onboarding failed for Clydesdale Bank", e);
        }

        return mutableMeans;
    }

    private void extractDataAndUpdateMeans(CybgGroupClientRegistration clientRegistration,
                                           Map<String, BasicAuthenticationMean> mutableMeans) {
        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                clientRegistration.getClientId());
        BasicAuthenticationMean clientSecretMean = new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(),
                clientRegistration.getClientSecret());
        mutableMeans.put(CybgGroupAuthMeansBuilderV2.CLIENT_ID_NAME, clientIdMean);
        mutableMeans.put(CybgGroupAuthMeansBuilderV2.CLIENT_SECRET_NAME, clientSecretMean);
    }
}
