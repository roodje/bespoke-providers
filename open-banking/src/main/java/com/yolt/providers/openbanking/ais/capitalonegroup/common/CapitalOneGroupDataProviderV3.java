package com.yolt.providers.openbanking.ais.capitalonegroup.common;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.RenderingType;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.capitalonegroup.capitalone.service.CapitalOneAutoOnboardingServiceV3;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.service.ais.fetchdata.CapitalOneGroupFetchDataServiceV3;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansMapper;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.capitalonegroup.common.auth.CapitalOneAuthMeansBuilderV3.CLIENT_ID_NAME;
import static com.yolt.providers.openbanking.ais.capitalonegroup.common.auth.CapitalOneAuthMeansBuilderV3.REGISTRATION_ACCESS_TOKEN_NAME;

public class CapitalOneGroupDataProviderV3 extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private static final List<String> AUTO_ON_BOARDING_UNNECESSARY_MEANS = Arrays.asList(
            CLIENT_ID_NAME,
            REGISTRATION_ACCESS_TOKEN_NAME);

    public static final TypedAuthenticationMeans REGISTRATION_ACCESS_TOKEN_STRING =
            new TypedAuthenticationMeans("Registration Access Token", NoWhiteCharacterStringType.getInstance(), RenderingType.ONE_LINE_STRING);

    private final CapitalOneGroupFetchDataServiceV3 capitalOneGroupFetchDataService;
    private final AuthenticationService authenticationService;
    private final HttpClientFactory httpClientFactory;
    private final TokenScope scope;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansMapper;
    private final AccessMeansMapper<AccessMeans> accessMeansMapper;
    private final CapitalOneAutoOnboardingServiceV3 capitalOneAutoOnboardingService;

    public CapitalOneGroupDataProviderV3(CapitalOneGroupFetchDataServiceV3 fetchDataService,
                                         AccountRequestService accountRequestService,
                                         AuthenticationService authenticationService,
                                         HttpClientFactory httpClientFactory,
                                         TokenScope scope, ProviderIdentification providerIdentification,
                                         Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansSupplier,
                                         Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                         AccessMeansMapper<AccessMeans> accessMeansMapper,
                                         Supplier<Optional<KeyRequirements>> signingKeyRequirementsSupplier,
                                         Supplier<Optional<KeyRequirements>> transportKeyRequirementsSupplier,
                                         Supplier<ConsentValidityRules> consentValidityRulesSupplier, CapitalOneAutoOnboardingServiceV3 capitalOneAutoOnboardingService) {
        super(fetchDataService,
                accountRequestService,
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                authenticationMeansSupplier,
                typedAuthenticationMeansSupplier,
                accessMeansMapper,
                signingKeyRequirementsSupplier,
                transportKeyRequirementsSupplier,
                consentValidityRulesSupplier);
        this.capitalOneGroupFetchDataService = fetchDataService;
        this.authenticationService = authenticationService;
        this.httpClientFactory = httpClientFactory;
        this.scope = scope;
        this.authenticationMeansMapper = authenticationMeansSupplier;
        this.accessMeansMapper = accessMeansMapper;
        this.capitalOneAutoOnboardingService = capitalOneAutoOnboardingService;
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        DefaultAuthMeans authenticationMeans = authenticationMeansMapper.apply(urlFetchData.getAuthenticationMeans());
        HttpClient httpClient = httpClientFactory.createHttpClient(urlFetchData.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());
        return capitalOneGroupFetchDataService.getAccountsAndTransactions(httpClient,
                authenticationMeans,
                urlFetchData.getTransactionsFetchStartTime(),
                accessMeansMapper.fromJson(urlFetchData.getAccessMeans().getAccessMeans()),
                urlFetchData.getPsuIpAddress());
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        AccessMeansDTO accessMeans = request.getAccessMeans();
        AccessMeans oAuthToken = accessMeansMapper.fromJson(accessMeans.getAccessMeans());
        DefaultAuthMeans authenticationMeans = authenticationMeansMapper.apply(request.getAuthenticationMeans());

        if (StringUtils.isEmpty(oAuthToken.getRefreshToken())) {
            throw new TokenInvalidException("Refresh token is missing, and access token is expired.");
        }

        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

        try {
            AccessMeans newOAuthToken = authenticationService.refreshAccessToken(httpClient, authenticationMeans,
                    oAuthToken.getUserId(), oAuthToken.getRefreshToken(), oAuthToken.getRedirectUri(), scope,
                    request.getSigner());

            String newOAuthTokenJSON = accessMeansMapper.toJson(newOAuthToken);
            return new AccessMeansDTO(
                    accessMeans.getUserId(),
                    newOAuthTokenJSON,
                    new Date(),
                    newOAuthToken.getExpireTime()
            );
        } catch (HttpStatusCodeException e) {
            if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode()) && e.getResponseBodyAsString().contains("Token was issued to a different client")) {
                throw new TokenInvalidException(String.format("Received error code %s. User has to start from login step", e.getStatusCode()));
            } else {
                throw e;
            }
        }
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
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(authenticationMeans);

        try {
            capitalOneAutoOnboardingService
                    .register(
                            httpClientFactory.createHttpClient(
                                    urlAutoOnboardingRequest.getRestTemplateManager(),
                                    authenticationMeansMapper.apply(authenticationMeans),
                                    getProviderIdentifierDisplayName()),
                            urlAutoOnboardingRequest)
                    .ifPresent(clientRegistration -> {
                        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                clientRegistration.getClientId());
                        TypedAuthenticationMeans registrationAccessTokenTypedAuthMean = new TypedAuthenticationMeans("Registration Access Token", NoWhiteCharacterStringType.getInstance(), RenderingType.ONE_LINE_STRING);
                        BasicAuthenticationMean registrationTokenMean = new BasicAuthenticationMean(registrationAccessTokenTypedAuthMean.getType(),
                                clientRegistration.getRegistrationAccessToken());
                        mutableMeans.put(CLIENT_ID_NAME, clientIdMean);
                        mutableMeans.put(REGISTRATION_ACCESS_TOKEN_NAME, registrationTokenMean);
                    });
            return mutableMeans;
        } catch (RestClientException | IllegalStateException | TokenInvalidException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Auto-onboarding failed for CapitalOne", e);
        }
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
