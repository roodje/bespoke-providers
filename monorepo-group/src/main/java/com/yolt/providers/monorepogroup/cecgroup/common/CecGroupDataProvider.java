package com.yolt.providers.monorepogroup.cecgroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.cecgroup.cec.CecProperties;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeansProducer;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.ProviderIdentification;
import com.yolt.providers.monorepogroup.cecgroup.common.http.CecGroupHttpClient;
import com.yolt.providers.monorepogroup.cecgroup.common.http.CecGroupHttpClientFactory;
import com.yolt.providers.monorepogroup.cecgroup.common.mapper.CecGroupDateConverter;
import com.yolt.providers.monorepogroup.cecgroup.common.service.CecGroupAuthorizationServiceV1;
import com.yolt.providers.monorepogroup.cecgroup.common.service.CecGroupFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CecGroupDataProvider implements UrlDataProvider {

    private final ProviderIdentification providerIdentification;
    private final CecGroupAuthenticationMeansProducer authenticationMeansProducer;
    private final CecGroupHttpClientFactory httpClientFactory;
    private final CecGroupAuthorizationServiceV1 authorizationService;
    private final CecGroupFetchDataService fetchDataService;
    private final CecGroupDateConverter dateConverter;
    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final CecProperties properties;
    private final ConsentValidityRules consentValidityRules;

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest request) {
        CecGroupAuthenticationMeans authMeans = authenticationMeansProducer.createAuthenticationMeans(request.getAuthenticationMeans(),
                getProviderIdentifierDisplayName());
        CecGroupHttpClient httpClient = httpClientFactory.createHttpClient(authMeans,
                request.getRestTemplateManager(),
                getProviderIdentifierDisplayName());
        String consentId = authorizationService.getConsentId(httpClient,
                authMeans,
                request.getSigner(),
                request.getPsuIpAddress(),
                request.getBaseClientRedirectUrl(),
                request.getState());
        String consentPageUrl = UriComponentsBuilder.fromUriString(properties.getBaseUrl() + "/oauthcec/oauth2/authorize")
                .queryParam("consentId", consentId)
                .queryParam("state", request.getState())
                .queryParam("response_type", "code")
                .queryParam("scope", String.format("AIS:%s", consentId))
                .queryParam("client_id", authMeans.getClientId())
                .build()
                .toUriString();
        return new RedirectStep(consentPageUrl, null, consentId);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest request) {
        CecGroupAuthenticationMeans authMeans = authenticationMeansProducer.createAuthenticationMeans(request.getAuthenticationMeans(),
                getProviderIdentifierDisplayName());
        CecGroupHttpClient httpClient = httpClientFactory.createHttpClient(authMeans,
                request.getRestTemplateManager(),
                getProviderIdentifierDisplayName());
        String authCode = extractAuthCode(request.getRedirectUrlPostedBackFromSite());
        CecGroupAccessMeans accessMeans = authorizationService.createAccessMeans(httpClient,
                authMeans.getClientId(),
                authMeans.getClientSecret(),
                request.getBaseClientRedirectUrl(),
                authCode,
                request.getProviderState());
        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(request.getUserId(),
                        toJson(accessMeans),
                        dateConverter.toDate(LocalDate.now(clock)),
                        dateConverter.toDate(accessMeans.getExpirationTimestamp()))
        );
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        CecGroupAuthenticationMeans authMeans = authenticationMeansProducer.createAuthenticationMeans(
                urlRefreshAccessMeans.getAuthenticationMeans(),
                getProviderIdentifierDisplayName());
        CecGroupHttpClient httpClient = httpClientFactory.createHttpClient(authMeans,
                urlRefreshAccessMeans.getRestTemplateManager(),
                getProviderIdentifierDisplayName());

        CecGroupAccessMeans oldAccessMeans = fromJson(urlRefreshAccessMeans.getAccessMeans().getAccessMeans());
        CecGroupAccessMeans accessMeans = authorizationService.refreshAccessMeans(httpClient,
                authMeans.getClientId(),
                oldAccessMeans);
        return new AccessMeansDTO(urlRefreshAccessMeans.getAccessMeans().getUserId(),
                toJson(accessMeans),
                dateConverter.toDate(LocalDate.now(clock)),
                dateConverter.toDate(accessMeans.getExpirationTimestamp()));
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        CecGroupAuthenticationMeans authMeans = authenticationMeansProducer.createAuthenticationMeans(
                urlFetchData.getAuthenticationMeans(),
                getProviderIdentifierDisplayName());
        CecGroupHttpClient httpClient = httpClientFactory.createHttpClient(authMeans,
                urlFetchData.getRestTemplateManager(),
                getProviderIdentifierDisplayName());
        return fetchDataService.fetchData(authMeans,
                fromJson(urlFetchData.getAccessMeans().getAccessMeans()),
                httpClient,
                urlFetchData.getSigner(),
                urlFetchData.getPsuIpAddress(),
                urlFetchData.getTransactionsFetchStartTime(),
                getProviderIdentifierDisplayName());
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
        return providerIdentification.getVersion();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authenticationMeansProducer.getTransportKeyRequirements();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return authenticationMeansProducer.getSigningKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return consentValidityRules;
    }

    private String toJson(CecGroupAccessMeans consentId) {
        try {
            return objectMapper.writeValueAsString(consentId);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }

    private CecGroupAccessMeans fromJson(String accessMeans) {
        try {
            return objectMapper.readValue(accessMeans, CecGroupAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to deserialize provider state");
        }
    }

    private String extractAuthCode(String redirectUrlPostedBackFromSite) {
        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        final String error = queryParams.get("error");
        if (StringUtils.isNotEmpty(error)) {
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectUrlPostedBackFromSite);
        }

        final String authorizationCode = queryParams.get("code");
        if (ObjectUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing data for key code.");
        }
        return authorizationCode;
    }
}
