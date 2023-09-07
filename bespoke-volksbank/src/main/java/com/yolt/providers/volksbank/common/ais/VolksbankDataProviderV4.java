package com.yolt.providers.volksbank.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.config.VolksbankBaseProperties;
import com.yolt.providers.volksbank.common.exception.LoginNotFoundException;
import com.yolt.providers.volksbank.common.model.VolksbankAccessMeans;
import com.yolt.providers.volksbank.common.model.VolksbankAccessTokenResponse;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientFactoryV2;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientV4;
import com.yolt.providers.volksbank.common.service.VolksbankAuthorizationServiceV4;
import com.yolt.providers.volksbank.common.service.VolksbankFetchDataServiceV4;
import com.yolt.providers.volksbank.common.util.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class VolksbankDataProviderV4 implements UrlDataProvider {

    private final VolksbankBaseProperties properties;
    private final VolksbankAuthorizationServiceV4 authorizationService;
    private final VolksbankFetchDataServiceV4 fetchDataService;
    private final VolksbankHttpClientFactoryV2 httpClientFactory;
    private final ProviderIdentification providerIdentification;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        VolksbankAuthenticationMeans authenticationMeans = VolksbankAuthenticationMeans.fromAuthenticationMeans(
                urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());
        try {
            VolksbankHttpClientV4 httpClient = createHttpClient(urlGetLogin.getRestTemplateManager(), authenticationMeans);
            String consentId = authorizationService.getConsentId(authenticationMeans, httpClient);
            MultiValueMap<String, String> varMap = new LinkedMultiValueMap<>();
            varMap.add("response_type", "code");
            varMap.add("consentId", consentId);
            varMap.add("scope", "AIS");
            varMap.add("state", urlGetLogin.getState());
            varMap.add("redirect_uri", urlGetLogin.getBaseClientRedirectUrl());
            varMap.add("client_id", authenticationMeans.getClientId());
            String authorizationUrl = UriComponentsBuilder.fromHttpUrl(
                    properties.getAuthorizationUrl()).queryParams(varMap).build().encode().toString();
            return new RedirectStep(authorizationUrl, consentId, consentId);
        } catch (TokenInvalidException e) {
            throw new LoginNotFoundException(e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        VolksbankAuthenticationMeans authenticationMeans = VolksbankAuthenticationMeans.fromAuthenticationMeans(
                urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        VolksbankHttpClientV4 httpClientV3 = createHttpClient(urlCreateAccessMeans.getRestTemplateManager(),
                authenticationMeans);

        try {
            String redirectUrl = getRedirectUrl(urlCreateAccessMeans);
            VolksbankAccessTokenResponse accessToken = authorizationService.createAccessToken(urlCreateAccessMeans,
                    redirectUrl, authenticationMeans, httpClientV3);

            String accessMeans = serializeAccessMeans(accessToken, redirectUrl, urlCreateAccessMeans.getProviderState());

            return new AccessMeansOrStepDTO(
                    new AccessMeansDTO(
                            urlCreateAccessMeans.getUserId(),
                            accessMeans,
                            new Date(),
                            Date.from(Instant.now(clock).plusSeconds(accessToken.getExpiresIn()))
                    )
            );
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e.getMessage());
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        VolksbankAuthenticationMeans authenticationMeans = VolksbankAuthenticationMeans.fromAuthenticationMeans(
                urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        AccessMeansDTO accessMeansDTO = urlRefreshAccessMeans.getAccessMeans();
        VolksbankAccessMeans volksbankAccessMeansDTO = deserializeAccessMeans(accessMeansDTO.getAccessMeans());

        VolksbankHttpClientV4 httpClient = createHttpClient(urlRefreshAccessMeans.getRestTemplateManager(),
                authenticationMeans);

        VolksbankAccessTokenResponse accessToken = authorizationService.createNewAccessTokenFromRefreshToken(
                volksbankAccessMeansDTO, authenticationMeans, httpClient);

        String accessMeans = serializeAccessMeans(accessToken, volksbankAccessMeansDTO.getRedirectUrl(), volksbankAccessMeansDTO.getConsentId());

        return new AccessMeansDTO(
                accessMeansDTO.getUserId(),
                accessMeans,
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(accessToken.getExpiresIn()))
        );
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        VolksbankAuthenticationMeans authenticationMeans = VolksbankAuthenticationMeans.fromAuthenticationMeans(
                urlFetchData.getAuthenticationMeans(), getProviderIdentifier());

        VolksbankAccessMeans accessMeansDTO = deserializeAccessMeans(
                urlFetchData.getAccessMeans().getAccessMeans());

        VolksbankHttpClientV4 httpClient = createHttpClient(urlFetchData.getRestTemplateManager(),
                authenticationMeans);

        return fetchDataService.fetchData(accessMeansDTO,
                urlFetchData.getTransactionsFetchStartTime(), getProviderIdentifierDisplayName(), httpClient);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return VolksbankAuthenticationMeans.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return VolksbankAuthenticationMeans.getTransportKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public final String getProviderIdentifier() {
        return providerIdentification.getProviderIdentifier();
    }

    @Override
    public final String getProviderIdentifierDisplayName() {
        return providerIdentification.getProviderDisplayName();
    }

    @Override
    public final ProviderVersion getVersion() {
        return providerIdentification.getProviderVersion();
    }

    private String getRedirectUrl(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return UrlUtils.truncateUrlQueryParameters(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite());
    }

    private String serializeAccessMeans(final VolksbankAccessTokenResponse accessToken,
                                        final String redirectUrl,
                                        final String consentId) {
        try {
            return objectMapper.writeValueAsString(new VolksbankAccessMeans(accessToken, redirectUrl, consentId));
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize oAuthToken");
        }
    }

    private VolksbankAccessMeans deserializeAccessMeans(final String accessMean) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMean, VolksbankAccessMeans.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to obtain token from access means");
        }
    }

    private VolksbankHttpClientV4 createHttpClient(RestTemplateManager restTemplateManager, VolksbankAuthenticationMeans authenticationMeans) {
        return httpClientFactory.createHttpClient(authenticationMeans, restTemplateManager, getProviderIdentifier());
    }
}
