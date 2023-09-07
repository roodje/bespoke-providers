package com.yolt.providers.ing.common.service;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.ing.common.IngSampleAuthenticationMeans;
import com.yolt.providers.ing.common.auth.*;
import com.yolt.providers.ing.common.config.IngProperties;
import com.yolt.providers.ing.common.dto.TestIngAuthData;
import com.yolt.providers.ing.common.dto.TestRedirectUrlResponse;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IngAuthenticationServiceV3Test {

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");

    private static final String EXAMPLE_URL = "https://example-url.com";
    private static final String EXAMPLE_URL_BANK_REDIRECT_URL = "https://myaccount.ing.com/granting/";
    private static final String EXAMPLE_REDIRECT_URL = "https://example.com";
    private static final String EXAMPLE_URI = "example-uri";
    private static final String EXAMPLE_TOKEN = "example-token";
    private static final String EXAMPLE_INCORRECT_TOKEN = "incorrect-token";
    private static final String EXAMPLE_STATE = "state";
    private static final String EXAMPLE_LOGIN_URL = "%s?client_id=%s&scope=%s&state=%s&redirect_uri=%s&response_type=code";
    private static final String EXAMPLE_CLIENT_ID = "5ca1ab1e-c0ca-c01a-cafe-154deadbea75";
    private static final String REDIRECT_SCOPE = "payment-accounts%3Abalances%3Aview%20payment-accounts%3Atransactions%3Aview";
    private static final String TOKEN_ENDPOINT = "/token";
    private static final String AUTHORIZATION_URL_ENDPOINT = "/authorization-server-url";
    private static final String COUNTRY_CODE = "NL";

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private IngProperties properties;
    @Mock
    private IngClientAwareRestTemplateService clientAwareRestTemplateService;
    private IngAuthenticationMeans authenticationMeans;
    private AuthenticationMeansReference authenticationMeansReference;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Clock clock;
    private IngAuthenticationServiceV3 subject;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = IngAuthenticationMeans.createIngAuthenticationMeans(new IngSampleAuthenticationMeans().getAuthenticationMeans(), "PROVIDER_IDENTIFIER");
        subject = new IngAuthenticationServiceV3(clientAwareRestTemplateService, new IngSigningUtil(), properties, COUNTRY_CODE, clock);
        authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);
    }

    @Test
    public void shouldReturnIngClientAccessMeansForGetClientAccessMeansWithCorrectData() {
        // given
        when(properties.getBaseUrl()).thenReturn(EXAMPLE_URL);
        when(properties.getOAuthTokenEndpoint()).thenReturn(TOKEN_ENDPOINT);
        when(clientAwareRestTemplateService.buildRestTemplate(any(IngAuthenticationMeans.class), any(RestTemplateManager.class))).thenReturn(restTemplate);
        when(restTemplate.postForEntity(eq(properties.getBaseUrl() + properties.getOAuthTokenEndpoint()), any(HttpEntity.class), eq(IngAuthData.class)))
                .thenReturn(ResponseEntity.ok(prepareProperTokenResponse()));
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());
        IngClientAccessMeans expectedTokenResponse = prepareProperClientAccessMeans();

        // when
        IngClientAccessMeans accessToken = subject.getClientAccessMeans(authenticationMeans, authenticationMeansReference, restTemplateManager, signer);

        // then
        assertThat(accessToken).isEqualTo(expectedTokenResponse);
    }

    @Test
    public void shouldReturnRedirectUrlForGetIngRedirectUrlWithCorrectData() throws TokenInvalidException {
        // given
        when(properties.getBaseUrl()).thenReturn(EXAMPLE_URL);
        when(properties.getAuthorizationUrlServerEndpoint()).thenReturn(AUTHORIZATION_URL_ENDPOINT);
        when(clientAwareRestTemplateService.buildRestTemplate(any(IngAuthenticationMeans.class), any(RestTemplateManager.class))).thenReturn(restTemplate);
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add("scope", REDIRECT_SCOPE);
        requestPayload.add("country_code", COUNTRY_CODE);
        requestPayload.add("redirect_uri", EXAMPLE_REDIRECT_URL);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + properties.getAuthorizationUrlServerEndpoint())
                .queryParams(requestPayload).build().toString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(RedirectUrlResponse.class)))
                .thenReturn(ResponseEntity.ok(prepareProperRedirectUrlResponse()));
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());
        RedirectUrlResponse expectedRedirectResponse = new TestRedirectUrlResponse(EXAMPLE_URL_BANK_REDIRECT_URL);

        // when
        RedirectUrlResponse redirectUrlResponse = subject.getIngRedirectUrl(prepareProperClientAccessMeans(), authenticationMeans, EXAMPLE_REDIRECT_URL, restTemplateManager, signer);

        // then
        assertThat(redirectUrlResponse.getLocation()).isEqualTo(expectedRedirectResponse.getLocation());
    }

    @Test
    public void shouldReturnLoginUrlForGetLoginUrlWithCorrectData() {
        // given
        String expectedLoginUrl = String.format(EXAMPLE_LOGIN_URL, EXAMPLE_URL_BANK_REDIRECT_URL, EXAMPLE_CLIENT_ID, REDIRECT_SCOPE, EXAMPLE_STATE, EXAMPLE_URI);

        // when
        String loginUrl = subject.getLoginUrl(EXAMPLE_CLIENT_ID, EXAMPLE_URL_BANK_REDIRECT_URL, EXAMPLE_URI, EXAMPLE_STATE);

        // then
        assertThat(loginUrl).isEqualTo(expectedLoginUrl);
    }

    @Test
    public void shouldReturnIngUserAccessMeansForGetUserTokenWithCorrectData() throws TokenInvalidException {
        // given
        when(properties.getBaseUrl()).thenReturn(EXAMPLE_URL);
        when(properties.getOAuthTokenEndpoint()).thenReturn(TOKEN_ENDPOINT);
        when(clientAwareRestTemplateService.buildRestTemplate(any(IngAuthenticationMeans.class), any(RestTemplateManager.class))).thenReturn(restTemplate);
        when(restTemplate.postForEntity(eq(properties.getBaseUrl() + properties.getOAuthTokenEndpoint()), any(HttpEntity.class), eq(IngAuthData.class)))
                .thenReturn(ResponseEntity.ok(prepareProperTokenResponse()));
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());
        IngUserAccessMeans expectedTokenResponse = prepareProperUserAccessMeans();

        // when
        IngUserAccessMeans oAuth2AccessToken = subject.getUserToken(prepareProperClientAccessMeans(),
                authenticationMeans, restTemplateManager, signer, EXAMPLE_TOKEN);

        // then
        assertThat(oAuth2AccessToken).isEqualTo(expectedTokenResponse);
    }

    @Test
    public void shouldThrowTokenInvalidExceptionForGetUserTokenWhenUnauthorizedError() {
        // given
        when(properties.getBaseUrl()).thenReturn(EXAMPLE_URL);
        when(properties.getOAuthTokenEndpoint()).thenReturn(TOKEN_ENDPOINT);
        when(clientAwareRestTemplateService.buildRestTemplate(any(IngAuthenticationMeans.class), any(RestTemplateManager.class))).thenReturn(restTemplate);
        when(restTemplate.postForEntity(eq(properties.getBaseUrl() + properties.getOAuthTokenEndpoint()), any(HttpEntity.class), eq(IngAuthData.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());

        // when
        ThrowableAssert.ThrowingCallable getUserTokenCallable = () -> subject.getUserToken(prepareProperClientAccessMeans(),
                authenticationMeans, restTemplateManager, signer, EXAMPLE_TOKEN);

        // then
        assertThatThrownBy(getUserTokenCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldReturnIngUserAccessMeansForRefreshOAuthTokenWithCorrectData() throws TokenInvalidException {
        // given
        IngUserAccessMeans expectedTokenResponse = prepareProperUserAccessMeans();
        when(properties.getBaseUrl()).thenReturn(EXAMPLE_URL);
        when(properties.getOAuthTokenEndpoint()).thenReturn(TOKEN_ENDPOINT);
        when(clientAwareRestTemplateService.buildRestTemplate(any(IngAuthenticationMeans.class), any(RestTemplateManager.class))).thenReturn(restTemplate);
        when(restTemplate.postForEntity(eq(properties.getBaseUrl() + properties.getOAuthTokenEndpoint()), any(HttpEntity.class), eq(IngAuthData.class)))
                .thenReturn(ResponseEntity.ok(prepareProperTokenResponse()));
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());

        // when
        IngUserAccessMeans oAuth2AccessToken = subject.refreshOAuthToken(prepareProperClientAccessMeans(), prepareProperUserAccessMeans(),
                authenticationMeans, restTemplateManager, signer);

        // then
        assertThat(oAuth2AccessToken).isEqualTo(expectedTokenResponse);
    }

    @Test
    public void shouldThrowTokenInvalidExceptionForRefreshOAuthTokenWhenUnauthorizedError() {
        // given
        when(properties.getBaseUrl()).thenReturn(EXAMPLE_URL);
        when(properties.getOAuthTokenEndpoint()).thenReturn(TOKEN_ENDPOINT);
        when(clientAwareRestTemplateService.buildRestTemplate(any(IngAuthenticationMeans.class), any(RestTemplateManager.class))).thenReturn(restTemplate);
        when(restTemplate.postForEntity(eq(properties.getBaseUrl() + properties.getOAuthTokenEndpoint()), any(HttpEntity.class), eq(IngAuthData.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());

        // when
        ThrowableAssert.ThrowingCallable refreshOAuthTokenCallable = () -> subject.refreshOAuthToken(prepareIncorrectClientAccessMeans(), prepareProperUserAccessMeans(),
                authenticationMeans, restTemplateManager, signer);

        // then
        assertThatThrownBy(refreshOAuthTokenCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    private IngClientAccessMeans prepareProperClientAccessMeans() {
        return new IngClientAccessMeans(prepareProperTokenResponse(), authenticationMeansReference, clock);
    }

    private IngClientAccessMeans prepareIncorrectClientAccessMeans() {
        return new IngClientAccessMeans(prepareIncorrectTokenResponse(), authenticationMeansReference, clock);
    }

    private IngUserAccessMeans prepareProperUserAccessMeans() {
        IngAuthData tokenResponse = prepareProperTokenResponse();
        return new IngUserAccessMeans(tokenResponse, new IngClientAccessMeans(tokenResponse, authenticationMeansReference, clock), clock);
    }

    private IngAuthData prepareProperTokenResponse() {
        TestIngAuthData tokenResponse = new TestIngAuthData();
        tokenResponse.setAccessToken(EXAMPLE_TOKEN);
        tokenResponse.setTokenType("Bearer");
        return tokenResponse;
    }

    private IngAuthData prepareIncorrectTokenResponse() {
        TestIngAuthData tokenResponse = new TestIngAuthData();
        tokenResponse.setAccessToken(EXAMPLE_INCORRECT_TOKEN);
        tokenResponse.setTokenType("Bearer");
        return tokenResponse;
    }

    private RedirectUrlResponse prepareProperRedirectUrlResponse() {
        return new TestRedirectUrlResponse(EXAMPLE_URL_BANK_REDIRECT_URL);
    }
}
