package com.yolt.providers.monorepogroup.qontogroup.common.service;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.TokenResponse;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoGroupProviderState;
import com.yolt.providers.monorepogroup.qontogroup.common.http.QontoGroupHttpClient;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultQontoGroupAuthenticationServiceTest {

    private final static String CLIENT_ID = "THE-CLIENT-ID";
    private final static String CLIENT_SECRET = "THE-CLIENT-SECRET";
    private final static QontoGroupAuthenticationMeans AUTH_MEANS = new QontoGroupAuthenticationMeans(null, CLIENT_ID, CLIENT_SECRET);
    private final static String AUTHORIZATION_URL = "https://authorizationurl.com";

    @Mock
    QontoGroupHttpClient httpClient;

    private final Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));

    private DefaultQontoGroupAuthenticationService authorizationService = new DefaultQontoGroupAuthenticationService(AUTHORIZATION_URL, clock);

    @Test
    void getLoginUrl() {
        //given
        var state = UUID.randomUUID().toString();
        var baseRedirectUrl = "https://redirect.com";
        var expectedRedirectUrl = UriComponentsBuilder.fromUriString(AUTHORIZATION_URL)
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", baseRedirectUrl)
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("scope", "organization.read offline_access")
                .toUriString();

        //when
        var result = authorizationService.getLoginUrl(AUTH_MEANS, baseRedirectUrl, state);

        //then
        assertThat(result).isEqualTo(expectedRedirectUrl);
    }

    @Test
    void shouldCreateAccessMeans() throws TokenInvalidException {
        //given
        var redirectUrlPostedBack = "https://redirect.com?code=12345";
        var baseRedirectUrl = "https://redirect.com";
        var tokenResponse = mock(TokenResponse.class);
        given(tokenResponse.getAccessToken()).willReturn("accessToken");
        given(tokenResponse.getRefreshToken()).willReturn("refreshToken");
        given(tokenResponse.getExpiresInSeconds()).willReturn(600L);
        given(httpClient.createToken(any(MultiValueMap.class))).willReturn(tokenResponse);
        var expectedProviderState = new QontoGroupProviderState("accessToken", "refreshToken", 1640995800000L);

        //when
        var result = authorizationService.createAccessMeans(AUTH_MEANS, httpClient, baseRedirectUrl, redirectUrlPostedBack);

        //then
        assertThat(result).isEqualTo(expectedProviderState);

    }

    @ParameterizedTest
    @CsvSource(value = {"https://redirect.com?error=rejected,redirectUrlPostedBackFromSite contains information about error",
            "https://redirect.com?message=nothingInterested,redirectUrlPostedBackFromSite doesn't contain code"})
    void shouldThrowIllegalStateExceptionWhenRedirectUrlIsMalformed(String redirectUrlPostedBack, String expectedErrorMessage) throws TokenInvalidException {
        //given
        var baseRedirectUrl = "https://redirect.com";

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.createAccessMeans(AUTH_MEANS, httpClient, baseRedirectUrl, redirectUrlPostedBack);

        //then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(call)
                .withMessage(expectedErrorMessage);
    }

    @Test
    void shouldThrowRethrowTokenInvalidException() throws TokenInvalidException {
        //given
        var redirectUrlPostedBack = "https://redirect.com?code=12345";
        var baseRedirectUrl = "https://redirect.com";
        given(httpClient.createToken(any(MultiValueMap.class))).willThrow(TokenInvalidException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.createAccessMeans(AUTH_MEANS, httpClient, baseRedirectUrl, redirectUrlPostedBack);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call);
    }

    @Test
    void shouldRefreshAccessMeans() throws TokenInvalidException {
        var oldProviderState = new QontoGroupProviderState("oldAccessToken", "oldRefreshToken", 1L);
        var tokenResponse = mock(TokenResponse.class);
        given(tokenResponse.getAccessToken()).willReturn("newAccessToken");
        given(tokenResponse.getRefreshToken()).willReturn("newRefreshToken");
        given(tokenResponse.getExpiresInSeconds()).willReturn(600L);
        given(httpClient.createToken(any(MultiValueMap.class))).willReturn(tokenResponse);
        var expectedRefreshProviderState = new QontoGroupProviderState("newAccessToken", "newRefreshToken", 1640995800000L);

        //when
        var result = authorizationService.refreshAccessMeans(AUTH_MEANS, oldProviderState, httpClient);

        //then
        assertThat(result).isEqualTo(expectedRefreshProviderState);
    }
}