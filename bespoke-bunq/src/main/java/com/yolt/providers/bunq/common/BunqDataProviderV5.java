package com.yolt.providers.bunq.common;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yolt.providers.bunq.beanconfig.BunqDetailsProvider;
import com.yolt.providers.bunq.common.auth.BunqApiContext;
import com.yolt.providers.bunq.common.auth.BunqApiContextAdapter;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqHttpClientFactory;
import com.yolt.providers.bunq.common.http.BunqHttpClientV5;
import com.yolt.providers.bunq.common.http.BunqHttpServiceV5;
import com.yolt.providers.bunq.common.service.authorization.BunqAuthorizationServiceV5;
import com.yolt.providers.bunq.common.service.autoonboarding.BunqAutoOnboardingServiceV2;
import com.yolt.providers.bunq.common.service.fetchdata.BunqAccountsAndTransactionsServiceV5;
import com.yolt.providers.bunq.common.util.HsmUtils;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2.SIGNING_CERTIFICATE;
import static com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2.SIGNING_PRIVATE_KEY_ID;
import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_5;

@Service
public class BunqDataProviderV5 implements UrlDataProvider, AutoOnboardingProvider {

    private static final Gson OBJECT_MAPPER = new GsonBuilder().registerTypeAdapter(BunqApiContext.class, new BunqApiContextAdapter()).create();

    private final BunqHttpClientFactory httpClientFactory;
    private final BunqAccountsAndTransactionsServiceV5 accountsAndTransactionsService;
    private final BunqAuthorizationServiceV5 authorizationService;
    private final BunqAutoOnboardingServiceV2 autoOnboardingService;
    private final BunqProperties properties;
    private final Clock clock;

    public BunqDataProviderV5(@Qualifier("BunqHttpClientFactory") BunqHttpClientFactory httpClientFactory,
                              @Qualifier("BunqAccountsAndTransactionsServiceV5") BunqAccountsAndTransactionsServiceV5 accountsAndTransactionsService,
                              @Qualifier("BunqAuthorizationServiceV5") BunqAuthorizationServiceV5 authorizationService,
                              @Qualifier("BunqAutoonboardingServiceV2") BunqAutoOnboardingServiceV2 autoOnboardingService,
                              BunqProperties properties,
                              Clock clock) {
        this.httpClientFactory = httpClientFactory;
        this.accountsAndTransactionsService = accountsAndTransactionsService;
        this.authorizationService = authorizationService;
        this.autoOnboardingService = autoOnboardingService;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public String getProviderIdentifier() {
        return BunqDetailsProvider.BUNQ_PROVIDER_IDENTIFIER;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return BunqDetailsProvider.BUNQ_PROVIDER_DISPLAY_NAME;
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_5;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return BunqAuthenticationMeansV2.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID, SIGNING_CERTIFICATE);
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws TokenInvalidException, ProviderFetchDataException {
        BunqApiContext userApiContext = apiContextFromAccessMeans(urlFetchData.getAccessMeans());
        BunqHttpServiceV5 httpServiceV5 = createBunqHttpService(urlFetchData.getRestTemplateManager());
        List<ProviderAccountDTO> providerAccountDTOS = accountsAndTransactionsService.fetchAccountsAndTransactionsForUser(userApiContext,
                httpServiceV5, urlFetchData.getTransactionsFetchStartTime());
        return new DataProviderResponse(providerAccountDTOS);
    }

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        String loginUrl = authorizationService.getLoginUrl(
                BunqAuthenticationMeansV2.fromAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), getProviderIdentifierDisplayName()).getClientId(),
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getState());
        return new RedirectStep(loginUrl);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        BunqAuthenticationMeansV2 authenticationMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifierDisplayName());
        BunqHttpServiceV5 httpServiceV5 = createBunqHttpService(urlCreateAccessMeans.getRestTemplateManager());
        BunqApiContext context = authorizationService.authorizeUserAndStartSession(authenticationMeans, urlCreateAccessMeans, httpServiceV5);
        return new AccessMeansOrStepDTO(accessMeansFromBunqApiContext(urlCreateAccessMeans.getUserId(), context));
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        BunqApiContext context = apiContextFromAccessMeans(urlRefreshAccessMeans.getAccessMeans());
        BunqHttpServiceV5 httpServiceV5 = createBunqHttpService(urlRefreshAccessMeans.getRestTemplateManager());
        BunqApiContext newContext = authorizationService.refreshSessionAtBunq(context, httpServiceV5);
        return accessMeansFromBunqApiContext(urlRefreshAccessMeans.getAccessMeans().getUserId(), newContext);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return autoOnboardingService.getAutoConfigureMeans();
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        BunqHttpServiceV5 httpServiceV5 = createBunqHttpService(urlAutoOnboardingRequest.getRestTemplateManager());
        return autoOnboardingService.registerProvider(urlAutoOnboardingRequest, httpServiceV5);
    }

    @Override
    public void removeAutoConfiguration(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        BunqHttpServiceV5 httpServiceV5 = createBunqHttpService(urlAutoOnboardingRequest.getRestTemplateManager());
        autoOnboardingService.removeCallbackFromProvider(urlAutoOnboardingRequest, getProviderIdentifierDisplayName(), httpServiceV5);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private BunqHttpServiceV5 createBunqHttpService(RestTemplateManager restTemplateManager) {
        BunqHttpClientV5 httpClient = httpClientFactory.createHttpClient(restTemplateManager, getProviderIdentifierDisplayName());
        return new BunqHttpServiceV5(properties, httpClient);
    }

    private BunqApiContext apiContextFromAccessMeans(final AccessMeansDTO accessMeansDTO) {
        return OBJECT_MAPPER.fromJson(accessMeansDTO.getAccessMeans(), BunqApiContext.class);
    }

    private AccessMeansDTO accessMeansFromBunqApiContext(final UUID userId, final BunqApiContext apiContext) {
        String serializedApiContext = OBJECT_MAPPER.toJson(apiContext);
        Date expiryDate = Date.from(Instant.now(clock).plusSeconds(apiContext.getExpiryTimeInSeconds()));
        return new AccessMeansDTO(userId, serializedApiContext, new Date(), expiryDate);
    }
}
