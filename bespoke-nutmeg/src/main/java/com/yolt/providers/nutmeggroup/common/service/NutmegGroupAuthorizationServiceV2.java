package com.yolt.providers.nutmeggroup.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.nutmeggroup.common.AuthenticationMeansV2;
import com.yolt.providers.nutmeggroup.common.OAuthConstants;
import com.yolt.providers.nutmeggroup.common.dto.TokenResponse;
import com.yolt.providers.nutmeggroup.common.rest.HttpClient;
import com.yolt.providers.nutmeggroup.common.rest.NutmegGroupRestTemplateServiceV2;
import com.yolt.providers.nutmeggroup.nutmeg.configuration.NutmegProperties;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static com.yolt.providers.nutmeggroup.common.utils.SerializationUtils.fromJson;
import static com.yolt.providers.nutmeggroup.common.utils.SerializationUtils.toJson;

@Service
public class NutmegGroupAuthorizationServiceV2 {

    private final NutmegGroupRestTemplateServiceV2 restTemplateService;
    private final NutmegProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public NutmegGroupAuthorizationServiceV2(final NutmegGroupRestTemplateServiceV2 restTemplateService,
                                             final NutmegProperties properties,
                                             @Qualifier("NutmegGroupObjectMapper") final ObjectMapper objectMapper,
                                             final Clock clock) {
        this.restTemplateService = restTemplateService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public AccessMeansOrStepDTO createAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeansRequest,
                                                  final String providerIdentifierDisplayName) {
        RestTemplateManager restTemplateManager = urlCreateAccessMeansRequest.getRestTemplateManager();
        AuthenticationMeansV2 authenticationMeans = AuthenticationMeansV2.getAuthenticationMeans(urlCreateAccessMeansRequest.getAuthenticationMeans(), providerIdentifierDisplayName);
        HttpClient httpClient = restTemplateService.createHttpClient(restTemplateManager);

        String clientId = authenticationMeans.getClientId();
        String code = retrieveCodeFromRedirectUrlPostedBackFromSite(urlCreateAccessMeansRequest.getRedirectUrlPostedBackFromSite());
        String codeVerifier = urlCreateAccessMeansRequest.getProviderState();
        String redirectUri = urlCreateAccessMeansRequest.getBaseClientRedirectUrl();
        String absoluteUrl = properties.getTokenUrl();

        try {
            TokenResponse tokenResponse = httpClient.getAccessToken(clientId, code, codeVerifier, redirectUri, absoluteUrl);
            String tokenResponseValue = toJson(objectMapper, tokenResponse);
            Instant createTime = Instant.now(clock);

            AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                    urlCreateAccessMeansRequest.getUserId(),
                    tokenResponseValue,
                    new Date(),
                    Date.from(createTime.plusSeconds(tokenResponse.getExpiresIn()))
            );

            return new AccessMeansOrStepDTO(accessMeansDTO);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e.getMessage());
        }
    }

    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest,
                                             final String providerIdentifierDisplayName) throws TokenInvalidException {
        RestTemplateManager restTemplateManager = urlRefreshAccessMeansRequest.getRestTemplateManager();
        AuthenticationMeansV2 authenticationMeans = AuthenticationMeansV2.getAuthenticationMeans(urlRefreshAccessMeansRequest.getAuthenticationMeans(), providerIdentifierDisplayName);
        HttpClient httpClient = restTemplateService.createHttpClient(restTemplateManager);
        AccessMeansDTO accessMeans = urlRefreshAccessMeansRequest.getAccessMeans();

        String clientId = authenticationMeans.getClientId();
        String refreshToken = fromJson(objectMapper, accessMeans.getAccessMeans()).getRefreshToken();
        String absoluteUrl = properties.getTokenUrl();

        TokenResponse tokenResponse = httpClient.refreshAccessToken(clientId, refreshToken, absoluteUrl);
        tokenResponse.setRefreshToken(refreshToken);

        String tokenResponseValue = toJson(objectMapper, tokenResponse);
        Instant createTime = Instant.now(clock);

        return new AccessMeansDTO(
                accessMeans.getUserId(),
                tokenResponseValue,
                new Date(),
                Date.from(createTime.plusSeconds(tokenResponse.getExpiresIn()))
        );
    }

    private String retrieveCodeFromRedirectUrlPostedBackFromSite(final String redirectUrlPostedBackFromSite) {
        Map<String, String> params = UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        String error = params.get("error");
        String code = params.get(OAuthConstants.Values.CODE);

        if (!StringUtils.isEmpty(error)) {
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectUrlPostedBackFromSite);
        }

        if (StringUtils.isEmpty(code)) {
            throw new MissingDataException("Missing data for key code.");
        }
        return code;
    }
}