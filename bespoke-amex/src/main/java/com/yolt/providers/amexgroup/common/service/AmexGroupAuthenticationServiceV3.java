package com.yolt.providers.amexgroup.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.amexgroup.common.AmexGroupConfigurationProperties;
import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeans;
import com.yolt.providers.amexgroup.common.dto.RevokeTokenResponse;
import com.yolt.providers.amexgroup.common.dto.TokenResponse;
import com.yolt.providers.amexgroup.common.dto.TokenResponses;
import com.yolt.providers.amexgroup.common.utils.AmexMacHeaderUtilsV2;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@RequiredArgsConstructor
public class AmexGroupAuthenticationServiceV3 implements AmexGroupAuthenticationService {

    private static final String CODES_SEPARATOR = ",";
    private static final String CLIENT_ID = "client_id";
    private static final String GRANT_TYPE = "grant_type";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String SCOPE = "scope_list";
    private static final String CODE = "code";
    private static final String AUTHENTICATION = "Authentication";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String AMEX_API_KEY = "x-amex-api-key";
    private static final String STATE = "state";

    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String GRANT_TYPE_REVOKE_TOKEN = "revoke";
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String AMEX_SCOPE = "MEMBER_ACCT_INFO,FINS_STP_DTLS,FINS_BAL_INFO,FINS_TXN_INFO";
    private static final String REQUEST_TYPE = "request_type";
    private static final String REQUEST_TYPE_SINGLE = "single";

    private final AmexGroupRestTemplateServiceV3 amexGroupRestTemplateService;
    private final ObjectMapper objectMapper;
    private final AmexGroupConfigurationProperties amexGroupConfigurationProperties;
    private final AmexMacHeaderUtilsV2 amexMacHeaderUtils;
    private final Clock clock;

    @Override
    public String getLoginInfo(AmexGroupAuthMeans amexGroupAuthMeans,
                               UrlGetLoginRequest urlGetLogin) {

        final MultiValueMap<String, String> params = getLoginInfoParams(amexGroupAuthMeans, urlGetLogin);
        return UriComponentsBuilder
                .fromHttpUrl(amexGroupConfigurationProperties.getAuthorizationBaseUrlV2())
                .queryParams(params)
                .toUriString();
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeansFromCsv(AmexGroupAuthMeans amexGroupAuthMeans,
                                                            UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                                            String csvAuthorizationCodes) {
        TokenResponses responses = new TokenResponses();
        String[] codes = csvAuthorizationCodes.split(CODES_SEPARATOR);
        for (String code : codes) {
            TokenResponse token = createNewAccessMeans(amexGroupAuthMeans, urlCreateAccessMeans, code);
            responses.getTokens().add(token);
        }
        return new AccessMeansOrStepDTO(mapToAccessMeans(urlCreateAccessMeans.getUserId(), responses, objectMapper, clock));
    }

    @Override
    public AccessMeansDTO getRefreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans,
                                                AmexGroupAuthMeans amexGroupAuthMeans) throws TokenInvalidException {

        final AccessMeansDTO accessMeans = urlRefreshAccessMeans.getAccessMeans();
        final UUID userId = urlRefreshAccessMeans.getAccessMeans().getUserId();
        final TokenResponses authTokens = mapToTokenResponses(accessMeans.getAccessMeans(), objectMapper);
        final TokenResponses refreshedAuthTokens = new TokenResponses();

        for (TokenResponse tokenResponse : authTokens.getTokens()) {
            TokenResponse refreshedTokenResponse = refreshAccessMeans(urlRefreshAccessMeans, amexGroupAuthMeans, tokenResponse);
            refreshedAuthTokens.getTokens().add(refreshedTokenResponse);
        }
        return mapToAccessMeans(userId, refreshedAuthTokens, objectMapper, clock);
    }

    @Override
    public void revokeUserToken(UrlOnUserSiteDeleteRequest request, AmexGroupAuthMeans amexGroupAuthMeans)
            throws TokenInvalidException {

        final AccessMeansDTO accessMeans = request.getAccessMeans();

        if (Objects.isNull(accessMeans)) {
            throw new TokenInvalidException("Cannot revoke user token - accessMeans not found in the request!");
        }

        final TokenResponses authTokens = mapToTokenResponses(accessMeans.getAccessMeans(), objectMapper);

        for (TokenResponse tokenResponse : authTokens.getTokens()) {
            revokeUserAccessToken(request, amexGroupAuthMeans, tokenResponse);
        }
    }

    private void revokeUserAccessToken(UrlOnUserSiteDeleteRequest onUserSiteDeleteRequest,
                                       AmexGroupAuthMeans amexGroupAuthMeans,
                                       TokenResponse authToken) throws TokenInvalidException {

        final RestTemplate restTemplate = amexGroupRestTemplateService.buildRestTemplate(
                amexGroupAuthMeans,
                onUserSiteDeleteRequest.getRestTemplateManager());
        final UUID userId = onUserSiteDeleteRequest.getAccessMeans().getUserId();
        final MultiValueMap<String, String> form = getRevokeTokenParams(authToken.getAccessToken());
        try {
            RevokeTokenResponse revokeTokenResponse = restTemplate.postForEntity(
                    "/apiplatform/v2/oauth/token_revocation/mac",
                    new HttpEntity<>(form, createHeaders(amexGroupAuthMeans, GRANT_TYPE_REVOKE_TOKEN, APPLICATION_FORM_URLENCODED)),
                    RevokeTokenResponse.class).getBody();

            if (!"success".equals(revokeTokenResponse.getResult())) {
                throw new TokenInvalidException(String.format("Token revocation for user %s failed!", userId));
            }
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                    throw new TokenInvalidException(String.format("Token revocation failed with HTTP status code %d for user %s", e.getStatusCode().value(), userId));

                default:
                    throw e;
            }
        }
    }

    private TokenResponse refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans,
                                             AmexGroupAuthMeans amexGroupAuthMeans,
                                             TokenResponse authToken) throws TokenInvalidException {
        final RestTemplate restTemplate = amexGroupRestTemplateService.buildRestTemplate(
                amexGroupAuthMeans,
                urlRefreshAccessMeans.getRestTemplateManager());
        final UUID userId = urlRefreshAccessMeans.getAccessMeans().getUserId();
        final MultiValueMap<String, String> form = getRefreshAccessMeansParams(authToken.getRefreshToken());
        try {
            return restTemplate.postForEntity(
                    "/apiplatform/v1/oauth/token/refresh/mac",
                    new HttpEntity<>(form, createHeaders(amexGroupAuthMeans, GRANT_TYPE_REFRESH_TOKEN, APPLICATION_FORM_URLENCODED)),
                    TokenResponse.class).getBody();

        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                    throw new TokenInvalidException(String.format("Token refresh failed with HTTP status code %d for user %s", e.getStatusCode().value(), userId));
                case BAD_REQUEST:
                    if(e.getResponseBodyAsString().contains("invalid_grant")) {
                        throw new TokenInvalidException(String.format("Invalid refresh token used hence token refresh failed with HTTP status code %d for user %s", e.getStatusCode().value(), userId));
                    }
                    throw e;
                default:
                    throw e;
            }
        }
    }

    private TokenResponse createNewAccessMeans(AmexGroupAuthMeans amexGroupAuthMeans,
                                               UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                               String authorizationCode) {
        final RestTemplate restTemplate = amexGroupRestTemplateService.buildRestTemplate(
                amexGroupAuthMeans,
                urlCreateAccessMeans.getRestTemplateManager());
        final MultiValueMap<String, String> form = getCreateNewAccessMeansParams(
                authorizationCode,
                urlCreateAccessMeans.getBaseClientRedirectUrl());
        try {
            HttpHeaders headers = createHeaders(amexGroupAuthMeans, GRANT_TYPE_AUTHORIZATION_CODE, APPLICATION_FORM_URLENCODED);
            return restTemplate.postForEntity(
                    "/apiplatform/v2/oauth/token/mac",
                    new HttpEntity<>(form, headers),
                    TokenResponse.class).getBody();
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                    throw new GetAccessTokenFailedException(String.format(
                            "Token exchange failed with HTTP status code %d for user %s",
                            e.getStatusCode().value(),
                            urlCreateAccessMeans.getUserId()));
                default:
                    throw e;
            }
        }
    }

    private MultiValueMap<String, String> getRefreshAccessMeansParams(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(GRANT_TYPE, GRANT_TYPE_REFRESH_TOKEN);
        form.add(REFRESH_TOKEN, refreshToken);
        return form;
    }

    private MultiValueMap<String, String> getRevokeTokenParams(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(GRANT_TYPE, GRANT_TYPE_REVOKE_TOKEN);
        form.add(ACCESS_TOKEN, refreshToken);
        form.add(REQUEST_TYPE, REQUEST_TYPE_SINGLE);
        return form;
    }

    private static TokenResponses mapToTokenResponses(String accessMeans, ObjectMapper objectMapper) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, TokenResponses.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Cannot parse access means to TokenResponse");
        }
    }

    private MultiValueMap<String, String> getLoginInfoParams(AmexGroupAuthMeans amexGroupAuthMeans,
                                                             UrlGetLoginRequest urlGetLogin) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(CLIENT_ID, amexGroupAuthMeans.getClientId());
        params.add(REDIRECT_URI, urlGetLogin.getBaseClientRedirectUrl());
        params.add(SCOPE, AMEX_SCOPE);
        params.add(STATE, urlGetLogin.getState());
        return params;
    }

    @SneakyThrows
    private HttpHeaders createHeaders(AmexGroupAuthMeans amexGroupAuthMeans, String grantType, MediaType mediaType) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add(AUTHENTICATION, amexMacHeaderUtils.generateAuthMacToken(
                amexGroupAuthMeans.getClientId(),
                amexGroupAuthMeans.getClientSecret(),
                grantType));
        headers.add(AMEX_API_KEY, amexGroupAuthMeans.getClientId());
        return headers;
    }

    private MultiValueMap<String, String> getCreateNewAccessMeansParams(String authorizationCode, String redirectUrl) {
        final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(CODE, authorizationCode);
        form.add(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
        form.add(SCOPE, AMEX_SCOPE);
        form.add(REDIRECT_URI, redirectUrl);
        return form;
    }

    private static AccessMeansDTO mapToAccessMeans(UUID userId, TokenResponses tokenResponses, ObjectMapper objectMapper, Clock clock) {
        try {
            final String accessMeansValue = objectMapper.writeValueAsString(tokenResponses);
            return new AccessMeansDTO(userId, accessMeansValue, new Date(), getExpiryDate(tokenResponses, clock));
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize authToken");
        }
    }

    private static Date getExpiryDate(TokenResponses tokenResponses, Clock clock) {
        Date expiryDate = null;
        for (TokenResponse tokenResponse : tokenResponses.getTokens()) {
            Date newExpiryDate = Date.from(Instant.now(clock).plusSeconds(tokenResponse.getExpiresIn()));
            if (expiryDate == null || newExpiryDate.before(expiryDate)) {
                expiryDate = newExpiryDate;
            }
        }
        if (expiryDate == null) {
            throw new GetAccessTokenFailedException("Cannot calculate expiry date");
        }
        return expiryDate;
    }
}
