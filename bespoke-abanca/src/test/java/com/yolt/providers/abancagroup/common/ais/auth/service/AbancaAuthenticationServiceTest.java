package com.yolt.providers.abancagroup.common.ais.auth.service;

import com.yolt.providers.abancagroup.common.AbancaSampleAuthenticationMeans;
import com.yolt.providers.abancagroup.common.ais.auth.dto.TestAbancaAuthData;
import com.yolt.providers.abancagroup.common.AbancaHttpClient;
import com.yolt.providers.abancagroup.common.AbancaHttpClientFactory;
import com.yolt.providers.abancagroup.common.ais.auth.AbancaAuthenticationMeans;
import com.yolt.providers.abancagroup.common.ais.auth.dto.AbancaTokens;
import com.yolt.providers.abancagroup.common.ais.config.AbancaGroupProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
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

import static com.yolt.providers.abancagroup.common.AbancaSampleAuthenticationMeans.API_KEY_SAMPLE;
import static com.yolt.providers.abancagroup.common.AbancaSampleAuthenticationMeans.CLIENT_ID_SAMPLE;
import static com.yolt.providers.abancagroup.common.ais.auth.AbancaAuthenticationMeans.createAuthenticationMeans;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbancaAuthenticationServiceTest {

    private static final String REDIRECT_URL = "redirectUrl";
    private static final String BASE_URL = "https://www.baseUrl.com";
    private static final String AUTH_CODE = "authCode";
    private static final String STATE = "state";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESHED_ACCESS_TOKEN = "refreshed_access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String REFRESHED_REFRESH_TOKEN = "refresh_token";
    private static final long EXPIRES_IN = 10000L;
    private static final long FIXED_TIMESTAMP = 1000000000L;


    @Mock
    private AbancaHttpClientFactory httpClientFactory;
    @Mock
    private AbancaGroupProperties properties;
    @Mock
    private Clock clock;
    @Mock
    private RestTemplateManager restTemplateManager;
    @Mock
    private AbancaHttpClient httpClient;

    private AbancaAuthenticationMeans authenticationMeans;
    private AbancaAuthenticationService subject;

    @BeforeEach
    public void setUp() throws IOException, URISyntaxException {
        authenticationMeans = createAuthenticationMeans(new AbancaSampleAuthenticationMeans().getAuthenticationMeans(), "PROVIDER");
        subject = new AbancaAuthenticationService(
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
        when(properties.getBaseUrl()).thenReturn(BASE_URL);
        //when
        String loginUrl = subject.getLoginUrl(CLIENT_ID_SAMPLE, REDIRECT_URL, STATE);
        //then
        assertThat(loginUrl).isEqualTo(BASE_URL +
                "/oauth/" + CLIENT_ID_SAMPLE + "/Abanca?" +
                "scope=Accounts Transactions&redirect_uri=" +
                REDIRECT_URL +
                "&state=" +
                STATE +
                "&response_type=code");
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnGetUserToken() throws TokenInvalidException {
        //given
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(FIXED_TIMESTAMP));
        when(httpClient.getUserToken(CLIENT_ID_SAMPLE, API_KEY_SAMPLE, AUTH_CODE))
                .thenReturn(Optional.of(new TestAbancaAuthData(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN)));
        //when
        AbancaTokens tokens = subject.getUserToken(authenticationMeans, restTemplateManager, AUTH_CODE);
        //then
        assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(tokens.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(tokens.getExpiryTimestamp()).isEqualTo(FIXED_TIMESTAMP + EXPIRES_IN * 1000);
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhenNullTokenIsReturnedOnGetUserToken() throws TokenInvalidException {
        //given
        when(httpClient.getUserToken(CLIENT_ID_SAMPLE, API_KEY_SAMPLE, AUTH_CODE)).thenReturn(Optional.empty());
        //when
        ThrowableAssert.ThrowingCallable getUserTokenCallable = () -> subject.getUserToken(authenticationMeans, restTemplateManager, AUTH_CODE);
        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(getUserTokenCallable)
                .withMessage("Empty body with token response");
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhenTokenInvalidExceptionIsThrownOnGetUserToken() throws TokenInvalidException {
        //given
        doThrow(new TokenInvalidException()).when(httpClient).getUserToken(CLIENT_ID_SAMPLE, API_KEY_SAMPLE, AUTH_CODE);
        //when
        ThrowableAssert.ThrowingCallable getUserTokenCallable = () -> subject.getUserToken(authenticationMeans, restTemplateManager, AUTH_CODE);
        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(getUserTokenCallable)
                .withMessage("Error during exchanging authorisation code to access token");
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnRefreshUserToken() throws TokenInvalidException {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(FIXED_TIMESTAMP));
        when(httpClient.refreshUserToken(CLIENT_ID_SAMPLE, API_KEY_SAMPLE, REFRESH_TOKEN))
                .thenReturn(Optional.of(new TestAbancaAuthData(REFRESHED_ACCESS_TOKEN, EXPIRES_IN, REFRESHED_REFRESH_TOKEN)));
        //when
        AbancaTokens tokens = subject.refreshUserToken(REFRESH_TOKEN, authenticationMeans, restTemplateManager);
        //then
        assertThat(tokens.getAccessToken()).isEqualTo(REFRESHED_ACCESS_TOKEN);
        assertThat(tokens.getRefreshToken()).isEqualTo(REFRESHED_REFRESH_TOKEN);
        assertThat(tokens.getExpiryTimestamp()).isEqualTo(FIXED_TIMESTAMP + EXPIRES_IN * 1000);
    }

    @Test
    void shouldThrowTokenInvalidExceptionExceptionWhenNullTokenIsReturnedOnRefreshUserToken() throws TokenInvalidException {
        when(httpClient.refreshUserToken(CLIENT_ID_SAMPLE, API_KEY_SAMPLE, REFRESH_TOKEN)).thenReturn(Optional.empty());
        //when
        ThrowableAssert.ThrowingCallable getUserTokenCallable = () -> subject.refreshUserToken(REFRESH_TOKEN, authenticationMeans, restTemplateManager);
        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(getUserTokenCallable)
                .withMessage("Empty body with token response");
    }
}