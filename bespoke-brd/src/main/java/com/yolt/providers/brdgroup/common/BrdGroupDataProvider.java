package com.yolt.providers.brdgroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.brdgroup.common.authorization.AuthorizationService;
import com.yolt.providers.brdgroup.common.config.ProviderIdentification;
import com.yolt.providers.brdgroup.common.fetchdata.FetchDataService;
import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClient;
import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClientFactory;
import com.yolt.providers.brdgroup.common.util.HsmUtils;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.brdgroup.common.BrdGroupAuthenticationMeans.TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.brdgroup.common.BrdGroupAuthenticationMeans.TRANSPORT_KEY_ID_NAME;

@Slf4j
@RequiredArgsConstructor
public class BrdGroupDataProvider implements UrlDataProvider {

    private static final String LOGIN_ID_FIELD = "LoginID";

    private final BrdGroupHttpClientFactory httpClientFactory;
    private final AuthorizationService authorizationService;
    private final FetchDataService fetchDataService;
    private final ProviderIdentification providerIdentification;
    private final ObjectMapper objectMapper;

    @Override
    public FormStep getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        return authorizationService.generateLoginIdForm();
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        BrdGroupAuthenticationMeans authenticationMeans = BrdGroupAuthenticationMeans.createAuthenticationMeans(
                urlCreateAccessMeans.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());

        BrdGroupHttpClient brdGroupHttpClient = httpClientFactory.createHttpClient(authenticationMeans,
                urlCreateAccessMeans.getRestTemplateManager(),
                providerIdentification.getProviderDisplayName());

        String psuLoginId = urlCreateAccessMeans.getFilledInUserSiteFormValues().get(LOGIN_ID_FIELD);

        return authorizationService.createAccessMeans(brdGroupHttpClient, urlCreateAccessMeans.getPsuIpAddress(), psuLoginId, urlCreateAccessMeans.getUserId());
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        throw new TokenInvalidException("BRD does not support refreshing tokens");
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        BrdGroupAuthenticationMeans authenticationMeans = BrdGroupAuthenticationMeans.createAuthenticationMeans(
                urlFetchData.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());

        BrdGroupHttpClient brdGroupHttpClient = httpClientFactory.createHttpClient(authenticationMeans,
                urlFetchData.getRestTemplateManager(),
                providerIdentification.getProviderDisplayName());

        return fetchDataService.fetchData(brdGroupHttpClient,
                toBrdGroupAccessMeans(urlFetchData.getAccessMeans().getAccessMeans()),
                urlFetchData.getTransactionsFetchStartTime(),
                urlFetchData.getPsuIpAddress(),
                providerIdentification.getProviderDisplayName());
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) {
        BrdGroupAuthenticationMeans authenticationMeans = BrdGroupAuthenticationMeans.createAuthenticationMeans(
                urlOnUserSiteDeleteRequest.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());

        BrdGroupHttpClient brdGroupHttpClient = httpClientFactory.createHttpClient(authenticationMeans,
                urlOnUserSiteDeleteRequest.getRestTemplateManager(),
                providerIdentification.getProviderDisplayName());

        authorizationService.deleteConsent(brdGroupHttpClient,
                toBrdGroupAccessMeans(urlOnUserSiteDeleteRequest.getAccessMeans().getAccessMeans()).getConsentId());
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentification.getProviderIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentification.getProviderDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return providerIdentification.getProviderVersion();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> authMeans = new HashMap<>();
        authMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        authMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return authMeans;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    @SneakyThrows(TokenInvalidException.class)
    private BrdGroupAccessMeans toBrdGroupAccessMeans(String accessMeans) {
        try {
            return objectMapper.readValue(accessMeans, BrdGroupAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Error converting access means");
        }
    }
}
