package com.yolt.providers.starlingbank.common;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.starlingbank.common.auth.HsmEIdasUtils;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthMeansSupplier;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClientFactoryV4;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpHeadersProducer;
import com.yolt.providers.starlingbank.common.http.signer.StarlingBankHttpSigner;
import com.yolt.providers.starlingbank.common.http.signer.StarlingBankHttpUnsupportedSigner;
import com.yolt.providers.starlingbank.common.service.authorization.StarlingBankAuthorizationService;
import com.yolt.providers.starlingbank.common.service.fetchdata.StarlingBankFetchDataService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans.*;

@RequiredArgsConstructor
public class StarlingBankDataProvider implements UrlDataProvider {

    private final StarlingBankHttpClientFactoryV4 httpClientFactory;
    private final StarlingBankAuthorizationService authorizationService;
    private final StarlingBankFetchDataService fetchDataService;
    private final StarlingBankAuthMeansSupplier authMeansSupplier;
    private final Clock clock;

    @Getter
    private final String providerIdentifier;
    @Getter
    private final String providerIdentifierDisplayName;
    @Getter
    private final ProviderVersion version;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(API_KEY_NAME_2, API_KEY_STRING);
        typedAuthMeans.put(API_SECRET_NAME_2, API_SECRET_STRING);
        typedAuthMeans.put(TRANSPORT_KEY_ID_NAME_2, KEY_ID);
        typedAuthMeans.put(TRANSPORT_CERTIFICATE_NAME_2, CERTIFICATE_PEM);
        return typedAuthMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmEIdasUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME_2, TRANSPORT_CERTIFICATE_NAME_2);
    }

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest request) {
        StarlingBankAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        return new RedirectStep(authorizationService.getLoginUrl(request.getBaseClientRedirectUrl(), request.getState(), authMeans));
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest request) {
        StarlingBankAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        StarlingBankHttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authMeans);
        return new AccessMeansOrStepDTO(authorizationService.createAccessMeans(httpClient, authMeans,
                request.getRedirectUrlPostedBackFromSite(), request.getUserId()));
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        StarlingBankAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        StarlingBankHttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authMeans);
        return authorizationService.refreshAccessMeans(httpClient, request.getAccessMeans(), authMeans);
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest request) throws TokenInvalidException, ProviderFetchDataException {
        StarlingBankAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        StarlingBankHttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authMeans);
        return fetchDataService.getAccountsAndTransactions(httpClient, request.getAccessMeans(), request.getTransactionsFetchStartTime());
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private StarlingBankAuthenticationMeans createAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans) {
        return authMeansSupplier.createAuthenticationMeans(basicAuthMeans, getProviderIdentifier());
    }

    private StarlingBankHttpClient createHttpClient(RestTemplateManager restTemplateManager, StarlingBankAuthenticationMeans authMeans) {
        StarlingBankHttpSigner unsupportedSigner = new StarlingBankHttpUnsupportedSigner();
        StarlingBankHttpHeadersProducer headersProducer = new StarlingBankHttpHeadersProducer(unsupportedSigner, clock);
        return httpClientFactory.createHttpClient(restTemplateManager, getProviderIdentifierDisplayName(), headersProducer, authMeans);
    }
}