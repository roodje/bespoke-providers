package com.yolt.providers.openbanking.ais.virginmoney;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
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
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansMapper;
import com.yolt.providers.openbanking.ais.virginmoney.service.VirginMoneyAutoOnboardingServiceV3;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.web.client.RestClientException;

import java.security.cert.CertificateException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;
import static com.yolt.providers.openbanking.ais.virginmoney.auth.VirginMoneyAuthMeansBuilderV4.CLIENT_ID_NAME;
import static com.yolt.providers.openbanking.ais.virginmoney.auth.VirginMoneyAuthMeansBuilderV4.CLIENT_SECRET_NAME;

public class VirginMoneyDataProviderV5 extends GenericBaseDataProvider implements AutoOnboardingProvider {

    private static final List<String> AUTO_ON_BOARDING_UNNECESSARY_MEANS =
            Arrays.asList(CLIENT_ID_NAME,
                    CLIENT_SECRET_NAME);
    private final VirginMoneyAutoOnboardingServiceV3 virginMoneyAutoOnboardingService;
    private final HttpClientFactory httpClientFactory;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansMapper;

    public VirginMoneyDataProviderV5(FetchDataService fetchDataService, AccountRequestService accountRequestService, AuthenticationService authenticationService, HttpClientFactory httpClientFactory, TokenScope scope, ProviderIdentification providerIdentification, Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans, Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier, AccessMeansMapper accessMeansMapper, Supplier<Optional<KeyRequirements>> getSigningKeyRequirements, Supplier<Optional<KeyRequirements>> getTransportKeyRequirements, Supplier<ConsentValidityRules> consentValidityRulesSupplier, VirginMoneyAutoOnboardingServiceV3 virginMoneyAutoOnboardingService) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification, getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansMapper, getSigningKeyRequirements, getTransportKeyRequirements, consentValidityRulesSupplier);
        this.httpClientFactory = httpClientFactory;
        this.authenticationMeansMapper = getAuthenticationMeans;
        this.virginMoneyAutoOnboardingService = virginMoneyAutoOnboardingService;
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
            virginMoneyAutoOnboardingService
                    .register(httpClientFactory.createHttpClient(urlAutoOnboardingRequest.getRestTemplateManager(), authenticationMeansMapper.apply(mutableMeans), getProviderIdentifierDisplayName()), urlAutoOnboardingRequest, getProviderIdentifier())
                    .ifPresent(clientRegistration -> {
                        BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                clientRegistration.getClientId());
                        BasicAuthenticationMean clientSecretMean = new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(),
                                clientRegistration.getClientSecret());
                        mutableMeans.put(CLIENT_ID_NAME, clientIdMean);
                        mutableMeans.put(CLIENT_SECRET_NAME, clientSecretMean);
                    });
        } catch (RestClientException | IllegalStateException | TokenInvalidException | CertificateException e) {
            throw new AutoOnboardingException(getProviderIdentifier(), "Auto-onboarding failed for Virgin Money Bank", e);
        }

        return mutableMeans;
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        // Virgin Money does not support refresh token. An access token is simply valid for 90 days. If expired, the user needs to relogin.
        throw new TokenInvalidException();
    }
}
