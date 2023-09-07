package com.yolt.providers.knabgroup.common.auth;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.knabgroup.common.configuration.KnabGroupProperties;
import com.yolt.providers.knabgroup.common.dto.external.AuthData;
import com.yolt.providers.knabgroup.common.dto.external.ConsentResponse;
import com.yolt.providers.knabgroup.common.dto.internal.KnabAccessMeans;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClient;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClientFactory;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KnabGroupAuthenticationServiceV2Test {

    private static final String RESPONSE_TYPE = "response_type";
    private static final String CODE = "code";
    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String STATE = "state";
    private static final String SCOPE = "scope";

    @Mock
    private KnabGroupHttpClientFactory httpClientFactory;
    @Mock
    private KnabSigningService signingService;
    @Mock
    private KnabGroupProperties properties;

    @Mock
    private KnabGroupHttpClient httpClient;
    @Mock
    private RestTemplateManager restTemplateManager;
    @Mock
    private Signer signer;

    private static final KnabGroupAuthenticationMeans AUTHENTICATION_MEANS = KnabGroupAuthenticationMeans.createKnabGroupAuthenticationMeans(SampleAuthenticationMeans.getSampleAuthenticationMeans(), "Knab Tests");
    private static final Clock clock = Clock.systemUTC();

    private KnabGroupAuthenticationServiceV2 authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new KnabGroupAuthenticationServiceV2(httpClientFactory, signingService, properties, clock);
    }

    @Test
    void shouldCreateConsent() {
        //given
        given(httpClientFactory.createKnabGroupHttpClient(restTemplateManager, AUTHENTICATION_MEANS)).willReturn(httpClient);
        AuthData clientToken = mock(AuthData.class);
        when(clientToken.getAccessToken()).thenReturn("awesome-client-token");
        given(httpClient.postForClientToken(any(HttpEntity.class))).willReturn(clientToken);
        ConsentResponse consentResponse = mock(ConsentResponse.class);
        when(consentResponse.getConsentId()).thenReturn("awesome-consent-id");
        when(signingService.calculateDigest(any())).thenReturn("digest");
        when(signingService.calculateSignature(any(), any(), any())).thenReturn("signature");
        given(httpClient.postForConsent(any(HttpEntity.class))).willReturn(consentResponse);

        //when
        String consentId = authenticationService.createConsent(AUTHENTICATION_MEANS, restTemplateManager, "127.0.0.1", "https://redirect-uri.com/callback", signer);

        //then
        assertThat(consentId).isEqualTo("awesome-consent-id");
    }

    @Test
    void shouldThrowGetLoginInfoUrlFailedExceptionWhenReceivedNullConsentResponse() {
        //given
        given(httpClientFactory.createKnabGroupHttpClient(restTemplateManager, AUTHENTICATION_MEANS)).willReturn(httpClient);
        AuthData clientToken = mock(AuthData.class);
        when(clientToken.getAccessToken()).thenReturn("awesome-client-token");
        given(httpClient.postForClientToken(any(HttpEntity.class))).willReturn(clientToken);
        when(signingService.calculateDigest(any())).thenReturn("digest");
        when(signingService.calculateSignature(any(), any(), any())).thenReturn("signature");

        //when
        ThrowingCallable callable = () -> authenticationService.createConsent(AUTHENTICATION_MEANS, restTemplateManager, "127.0.0.1", "https://redirect-uri.com/callback", signer);
        ;

        //then
        assertThatExceptionOfType(GetLoginInfoUrlFailedException.class)
                .isThrownBy(callable)
                .withMessage("Empty body with consent response");
    }

    @Test
    void shouldReturnLoginUrl() {
        //given
        String clientId = "client-id";
        String consentId = "consent-id";
        String redirectUri = "https://redirect-uri.com/callback";
        String state = UUID.randomUUID().toString();
        given(properties.getAuthorizationUrl()).willReturn("https://knab.io/");
        String expectedLoginUrl = UriComponentsBuilder.fromHttpUrl("https://knab.io/connect/authorize")
                .queryParam(RESPONSE_TYPE, CODE)
                .queryParam(CLIENT_ID, clientId)
                .queryParam(REDIRECT_URI, redirectUri)
                .queryParam(SCOPE, "psd2 offline_access AIS:consent-id")
                .queryParam(STATE, state)
                .build().toString();

        //when
        String loginUrl = authenticationService.getClientLoginUrl(clientId, consentId, redirectUri, state);

        //then
        assertThat(loginUrl).isEqualTo(expectedLoginUrl);
    }

    @Test
    void shouldReturnUserAuthData() {
        //given
        String authorizationCode = "awesome-authorization-code";
        String redirectUri = "https://redirect-uri.com/callback";
        given(httpClientFactory.createKnabGroupHttpClient(restTemplateManager, AUTHENTICATION_MEANS)).willReturn(httpClient);
        AuthData userAuthData = mock(AuthData.class);
        when(userAuthData.getAccessToken()).thenReturn("user-access-token");
        when(userAuthData.getRefreshToken()).thenReturn("user-refresh-token");
        when(userAuthData.getExpiresIn()).thenReturn(3600L);
        when(userAuthData.getScope()).thenReturn("psd2 offline_access AIS:6f6e6568-956b-40ea-95f0-7e2da0aaa123");
        when(userAuthData.getTokenType()).thenReturn("token-type");
        when(httpClient.postForUserTokenWithAuthorizationCode(any(HttpEntity.class))).thenReturn(userAuthData);

        //when
        KnabAccessMeans knabAccessMeans = authenticationService.createAccessMeans(authorizationCode, redirectUri, restTemplateManager, AUTHENTICATION_MEANS);

        //then
        assertThat(knabAccessMeans).extracting(
                KnabAccessMeans::getAccessToken,
                KnabAccessMeans::getRefreshToken,
                KnabAccessMeans::getScope,
                KnabAccessMeans::getTokenType).contains(
                "user-access-token",
                "user-refresh-token",
                "psd2 offline_access AIS:6f6e6568-956b-40ea-95f0-7e2da0aaa123",
                "token-type");
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhenEmptyAuthDataWasReturned() {
        //given
        String authorizationCode = "awesome-authorization-code";
        String redirectUri = "https://redirect-uri.com/callback";
        given(httpClientFactory.createKnabGroupHttpClient(restTemplateManager, AUTHENTICATION_MEANS)).willReturn(httpClient);

        //when
        ThrowingCallable callable = () -> authenticationService.createAccessMeans(authorizationCode, redirectUri, restTemplateManager, AUTHENTICATION_MEANS);

        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(callable)
                .withMessage("Empty body with token response");
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhenConsentIdIsMissingInAuthData() {
        //given
        String authorizationCode = "awesome-authorization-code";
        String redirectUri = "https://redirect-uri.com/callback";
        given(httpClientFactory.createKnabGroupHttpClient(restTemplateManager, AUTHENTICATION_MEANS)).willReturn(httpClient);
        AuthData userAuthData = mock(AuthData.class);
        when(userAuthData.getScope()).thenReturn("offline_access");
        when(httpClient.postForUserTokenWithAuthorizationCode(any(HttpEntity.class))).thenReturn(userAuthData);

        //when
        ThrowingCallable callable = () -> authenticationService.createAccessMeans(authorizationCode, redirectUri, restTemplateManager, AUTHENTICATION_MEANS);

        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(callable)
                .withMessage("Missing consentId in scope of user accessToken");
    }

    @Test
    void shouldReturnRefreshedUserAuthData() throws TokenInvalidException {
        //given
        String refreshToken = "user-refresh-token";
        given(httpClientFactory.createKnabGroupHttpClient(restTemplateManager, AUTHENTICATION_MEANS)).willReturn(httpClient);
        AuthData userAuthData = mock(AuthData.class);
        when(userAuthData.getAccessToken()).thenReturn("new-user-access-token");
        when(userAuthData.getRefreshToken()).thenReturn("new-user-refresh-token");
        when(userAuthData.getExpiresIn()).thenReturn(3600L);
        when(userAuthData.getScope()).thenReturn("some-scope");
        when(userAuthData.getTokenType()).thenReturn("token-type");
        when(httpClient.postForUserTokenWithRefreshToken(any(HttpEntity.class))).thenReturn(userAuthData);

        //when
        KnabAccessMeans knabAccessMeans = authenticationService.refreshAccessMeans(refreshToken, restTemplateManager, AUTHENTICATION_MEANS);

        //then
        assertThat(knabAccessMeans).extracting(
                KnabAccessMeans::getAccessToken,
                KnabAccessMeans::getRefreshToken,
                KnabAccessMeans::getScope,
                KnabAccessMeans::getTokenType).contains(
                "new-user-access-token",
                "new-user-refresh-token",
                "some-scope",
                "token-type");
    }
}