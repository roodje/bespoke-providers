package com.yolt.providers.raiffeisenbank.common.ais.auth.service;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.raiffeisenbank.common.RaiffeisenBankHttpClient;
import com.yolt.providers.raiffeisenbank.common.RaiffeisenBankHttpClientFactory;
import com.yolt.providers.raiffeisenbank.common.RaiffeisenBankSampleAuthenticationMeans;
import com.yolt.providers.raiffeisenbank.common.ais.auth.RaiffeisenBankAuthenticationMeans;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.RaiffeisenBankTokens;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.TestRaiffeisenAuthData;
import com.yolt.providers.raiffeisenbank.common.ais.config.RaiffeisenBankProperties;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static com.yolt.providers.raiffeisenbank.common.RaiffeisenBankSampleAuthenticationMeans.CLIENT_ID_SAMPLE;
import static com.yolt.providers.raiffeisenbank.common.RaiffeisenBankSampleAuthenticationMeans.CLIENT_SECRET_SAMPLE;
import static com.yolt.providers.raiffeisenbank.common.ais.auth.RaiffeisenBankAuthenticationMeans.createAuthenticationMeans;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaiffeisenBankAuthenticationServiceTest {

    private static final String CONSENT_ID = "consentId";
    private static final String REDIRECT_URL = "redirectUrl";
    private static final String BASE_OAUTH_URL = "https://www.baseOauthUrl.com";
    private static final String AUTH_CODE = "authCode";
    private static final String STATE = "state";
    private static final String PSU_IP = "psuIp";
    private static final String IBAN = "iban";
    private static final String ACCOUNT_LOGIN = "accountLogin";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESHED_ACCESS_TOKEN = "refreshed_access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String REFRESHED_REFRESH_TOKEN = "refresh_token";
    private static final long EXPIRES_IN = 10000L;
    private static final long FIXED_TIMESTAMP = 1000000000L;


    @Mock
    private RaiffeisenBankHttpClientFactory httpClientFactory;
    @Mock
    private RaiffeisenBankProperties properties;
    @Mock
    private Clock clock;
    @Mock
    private RestTemplateManager restTemplateManager;
    @Mock
    private RaiffeisenBankHttpClient httpClient;

    private RaiffeisenBankAuthenticationMeans authenticationMeans;
    private RaiffeisenBankAuthenticationService subject;

    @BeforeEach
    public void setUp() throws IOException, URISyntaxException {
        authenticationMeans = createAuthenticationMeans(new RaiffeisenBankSampleAuthenticationMeans().getAuthenticationMeans(), "PROVIDER");
        subject = new RaiffeisenBankAuthenticationService(
                httpClientFactory,
                properties,
                clock
        );
        lenient().when(httpClientFactory.buildHttpClient(any(), any(), any()))
                .thenReturn(httpClient);
    }

    @Test
    void shouldReturnProperRedirectUrlWhenCorrectDataIsProvidedOnGetLoginUrl() {
        //given
        when(properties.getOAuthBaseUrl()).thenReturn(BASE_OAUTH_URL);
        //when
        String loginUrl = subject.getLoginUrl(CLIENT_ID_SAMPLE, CONSENT_ID, REDIRECT_URL, STATE);
        //then
        assertThat(loginUrl).isEqualTo(BASE_OAUTH_URL +
                "/psd2-rbro-oauth2-api/aisp/oauth2/authorize?client_id=" +
                CLIENT_ID_SAMPLE +
                "&scope=AISP&redirect_uri=" +
                REDIRECT_URL +
                "&state=" +
                STATE +
                "&response_type=code&consentId=" +
                CONSENT_ID);
    }

    @Test
    void shouldReturnProperConsentIdWhenCorrectDataIsProvidedOnCreateConsentId() throws TokenInvalidException {
        //given
        when(httpClient.createConsentId(CLIENT_ID_SAMPLE.toString(), REDIRECT_URL, PSU_IP, IBAN, ACCOUNT_LOGIN)).thenReturn(Optional.of(CONSENT_ID));
        //when
        var consentId = subject.createConsentId(authenticationMeans, REDIRECT_URL, PSU_IP, IBAN, ACCOUNT_LOGIN, restTemplateManager);
        //then
        assertTrue(consentId.isPresent());
        assertThat(consentId.get()).isEqualTo(CONSENT_ID);
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnGetUserToken() throws TokenInvalidException {
        //given
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(FIXED_TIMESTAMP));
        when(httpClient.getUserToken(CLIENT_ID_SAMPLE.toString(), CLIENT_SECRET_SAMPLE, AUTH_CODE, REDIRECT_URL))
                .thenReturn(Optional.of(new TestRaiffeisenAuthData(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN)));
        //when
        RaiffeisenBankTokens tokens = subject.getUserToken(authenticationMeans, restTemplateManager, AUTH_CODE, REDIRECT_URL);
        //then
        assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(tokens.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(tokens.getExpiryTimestamp()).isEqualTo(FIXED_TIMESTAMP + EXPIRES_IN * 1000);
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionExceptionWhenNullTokenIsReturnedOnGetUserToken() throws TokenInvalidException {
        //given
        when(httpClient.getUserToken(CLIENT_ID_SAMPLE.toString(), CLIENT_SECRET_SAMPLE, AUTH_CODE, REDIRECT_URL)).thenReturn(Optional.empty());
        //when
        ThrowableAssert.ThrowingCallable getUserTokenCallable = () -> subject.getUserToken(authenticationMeans, restTemplateManager, AUTH_CODE, REDIRECT_URL);
        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(getUserTokenCallable)
                .withMessage("Empty body with token response");
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhenTokenInvalidExceptionIsThrownOnGetUserToken() throws TokenInvalidException {
        //given
        doThrow(new TokenInvalidException()).when(httpClient).getUserToken(CLIENT_ID_SAMPLE.toString(), CLIENT_SECRET_SAMPLE, AUTH_CODE, REDIRECT_URL);
        //when
        ThrowableAssert.ThrowingCallable getUserTokenCallable = () -> subject.getUserToken(authenticationMeans, restTemplateManager, AUTH_CODE, REDIRECT_URL);
        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(getUserTokenCallable)
                .withMessage("Error during exchanging authorisation code to access token");
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnRefreshUserToken() throws TokenInvalidException {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(FIXED_TIMESTAMP));
        when(httpClient.refreshUserToken(CLIENT_ID_SAMPLE.toString(), CLIENT_SECRET_SAMPLE, REFRESH_TOKEN))
                .thenReturn(Optional.of(new TestRaiffeisenAuthData(REFRESHED_ACCESS_TOKEN, EXPIRES_IN, REFRESHED_REFRESH_TOKEN)));
        //when
        RaiffeisenBankTokens tokens = subject.refreshUserToken(REFRESH_TOKEN, authenticationMeans, restTemplateManager);
        //then
        assertThat(tokens.getAccessToken()).isEqualTo(REFRESHED_ACCESS_TOKEN);
        assertThat(tokens.getRefreshToken()).isEqualTo(REFRESHED_REFRESH_TOKEN);
        assertThat(tokens.getExpiryTimestamp()).isEqualTo(FIXED_TIMESTAMP + EXPIRES_IN * 1000);
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionExceptionWhenNullTokenIsReturnedOnRefreshUserToken() throws TokenInvalidException {
        when(httpClient.refreshUserToken(CLIENT_ID_SAMPLE.toString(), CLIENT_SECRET_SAMPLE, REFRESH_TOKEN)).thenReturn(Optional.empty());
        //when
        ThrowableAssert.ThrowingCallable getUserTokenCallable = () -> subject.refreshUserToken(REFRESH_TOKEN, authenticationMeans, restTemplateManager);
        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(getUserTokenCallable)
                .withMessage("Empty body with token response");
    }

    @Test
    void shouldProperlyCallEndpointWhenCorrectDataIsProvidedOnDeleteConsent() throws TokenInvalidException {
        //when
        subject.deleteConsent(CONSENT_ID, ACCESS_TOKEN, authenticationMeans, restTemplateManager);
        //then
        verify(httpClient).deleteConsent(CONSENT_ID, ACCESS_TOKEN, CLIENT_ID_SAMPLE.toString());
    }
}