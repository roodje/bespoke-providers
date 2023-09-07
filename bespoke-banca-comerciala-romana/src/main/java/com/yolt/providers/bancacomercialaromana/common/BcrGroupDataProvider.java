package com.yolt.providers.bancacomercialaromana.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancacomercialaromana.common.auth.BcrGroupAuthenticationMeans;
import com.yolt.providers.bancacomercialaromana.common.http.BcrGroupHttpClient;
import com.yolt.providers.bancacomercialaromana.common.http.BcrGroupHttpClientFactory;
import com.yolt.providers.bancacomercialaromana.common.model.Token;
import com.yolt.providers.bancacomercialaromana.common.service.BcrGroupAuthorizationServiceV1;
import com.yolt.providers.bancacomercialaromana.common.service.BcrGroupFetchDataServiceV1;
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
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.bancacomercialaromana.common.auth.BcrGroupAuthenticationMeans.fromAuthenticationMeans;
import static com.yolt.providers.bancacomercialaromana.common.service.BcrGroupAuthorizationServiceV1.CODE_NAME;

@RequiredArgsConstructor
public abstract class BcrGroupDataProvider implements UrlDataProvider {

    private final ObjectMapper objectMapper;
    private final BcrGroupAuthorizationServiceV1 authorizationService;
    private final BcrGroupFetchDataServiceV1 fetchDataService;
    private final BcrGroupHttpClientFactory httpClientFactory;
    private final String s3BaseUrl;
    private final Clock clock;

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        BcrGroupAuthenticationMeans clientConfiguration = fromAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());

        String loginUrl = authorizationService
                .getLoginUrl(clientConfiguration.getClientId(), urlGetLogin.getBaseClientRedirectUrl(), urlGetLogin.getState());

        return new RedirectStep(loginUrl);
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws TokenInvalidException, ProviderFetchDataException {
        BcrGroupAuthenticationMeans authenticationMeans = fromAuthenticationMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier());
        BcrGroupHttpClient httpClient = httpClientFactory.getHttpClient(
                authenticationMeans,
                urlFetchData.getRestTemplateManager(),
                getProviderIdentifier());

        return fetchDataService.fetchData(httpClient, urlFetchData);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        String authorizationCode = UriComponentsBuilder
                .fromUriString(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite())
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(CODE_NAME);

        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing authorization code in redirect url query parameters");
        }
        String redirectUrlWithoutQueryParams = urlCreateAccessMeans.getBaseClientRedirectUrl();

        UUID userId = urlCreateAccessMeans.getUserId();
        BcrGroupAuthenticationMeans authenticationMeans = fromAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        BcrGroupHttpClient httpClient = httpClientFactory
                .getHttpClient(authenticationMeans, urlCreateAccessMeans.getRestTemplateManager(), getProviderIdentifier());
        try {
            Token oAuthToken = authorizationService
                    .getAccessTokenUsingAuthorizationCode(httpClient, redirectUrlWithoutQueryParams, authorizationCode, authenticationMeans);
            return new AccessMeansOrStepDTO(toAccessMeans(userId, oAuthToken));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Failed to get access token", e);
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        AccessMeansDTO accessMeansDTO = urlRefreshAccessMeans.getAccessMeans();
        Token oAuthToken = toOAuthToken(accessMeansDTO.getAccessMeans());
        UUID userId = accessMeansDTO.getUserId();

        BcrGroupAuthenticationMeans authenticationMeans = fromAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        BcrGroupHttpClient httpClient = httpClientFactory
                .getHttpClient(authenticationMeans, urlRefreshAccessMeans.getRestTemplateManager(), getProviderIdentifier());

        Token refreshedOAuthToken = authorizationService.getAccessTokenUsingRefreshToken(httpClient, oAuthToken.getRefreshToken(), authenticationMeans);
        if (StringUtils.isEmpty(refreshedOAuthToken.getRefreshToken())) {
            refreshedOAuthToken.setRefreshToken(oAuthToken.getRefreshToken());
        }

        return toAccessMeans(userId, refreshedOAuthToken);
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(BcrGroupAuthenticationMeans.signingKeyRequirements);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(BcrGroupAuthenticationMeans.transportKeyRequirements);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return BcrGroupAuthenticationMeans.typedAuthenticationMeans;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return new ConsentValidityRules(Set.of("BCR"));
    }

    private AccessMeansDTO toAccessMeans(final UUID userId, final Token oAuthToken) {
        Date expirationDate = Date.from(Instant.now(clock).plusSeconds(oAuthToken.getExpiresIn()));
        try {
            return new AccessMeansDTO(userId, objectMapper.writeValueAsString(oAuthToken), new Date(), expirationDate);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Couldn't parse OAuth Token to JSON", e);
        }
    }

    private Token toOAuthToken(String accessMeans) {
        try {
            return objectMapper.readValue(accessMeans, Token.class);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Couldn't parse access means to Token object", e);
        }
    }
}
