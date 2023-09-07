package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankVanBredaGroupAuthenticationHttpClientTest {

    private static final String PROVIDER = "provider";
    private static final long EXPIRES_IN = 10L;
    private static final String ACCESS_TOKEN = "access_token";
    private static final String BASE_URL = "https://www.baseUrl.com";
    private static final String REFRESHED_ACCESS_TOKEN = "refreshed_access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String AUTH_CODE = "authCode";
    private static final String TPP_ID = "tppId";
    private static final String CODE_VERIFIER = "codeVerifier";
    private static final String SCOPE = "scope";
    private static final String CONSENT_ID = "consentId";
    private static final String SCA_OAUTH = "scaOAuth";
    private static final String REDIRECT_URL = "redirect.com";
    private static final String PSU_IP = "PSU_IP";

    @Mock
    private HttpErrorHandlerV2 errorHandler;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Clock clock;
    @Captor
    private ArgumentCaptor<HttpEntity<BankVanBredaGroupConsentRequest>> httpEntityCaptor;

    private ObjectMapper objectMapper;

    private BankVanBredaGroupAuthenticationHttpClient subject;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        MeterRegistry meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
        subject = new BankVanBredaGroupAuthenticationHttpClient(errorHandler, meterRegistry, restTemplate, clock, PROVIDER);
    }

    @Test
    void shouldReturnProperConsentWhenCorrectDataIsProvidedOnGetConsent() {
        //given
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(restTemplate.exchange(
                eq("/berlingroup/v1/consents"),
                eq(HttpMethod.POST),
                httpEntityCaptor.capture(),
                eq(BankVanBredaGroupConsent.class))
        ).thenReturn(ResponseEntity.ok(new TestBankVanBredaGroupConsent(CONSENT_ID, SCA_OAUTH)));

        //when
        BankVanBredaGroupConsent consent = subject.getConsent(REDIRECT_URL, PSU_IP, PROVIDER);
        //then

        assertThat(toJson(httpEntityCaptor.getValue().getBody())).isEqualTo(toJson(new BankVanBredaGroupConsentRequest(clock)));
        assertThat(consent.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(consent.scaOAuth()).isEqualTo(SCA_OAUTH);
    }

    @Test
    void shouldThrowExceptionWhenIncorrectBodyIsReceivedOnGetConsent() {
        //given
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(restTemplate.exchange(
                eq("/berlingroup/v1/consents"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BankVanBredaGroupConsent.class))
        )
                .thenReturn(ResponseEntity.ok(new TestBankVanBredaGroupConsent(CONSENT_ID, null)))
                .thenReturn(ResponseEntity.ok(new TestBankVanBredaGroupConsent(null, SCA_OAUTH)))
                .thenReturn(ResponseEntity.ok(null));

        //when
        ThrowableAssert.ThrowingCallable consent1Throwable = () -> subject.getConsent(REDIRECT_URL, PSU_IP, PROVIDER);
        ThrowableAssert.ThrowingCallable consent2Throwable = () -> subject.getConsent(REDIRECT_URL, PSU_IP, PROVIDER);
        ThrowableAssert.ThrowingCallable consent3Throwable = () -> subject.getConsent(REDIRECT_URL, PSU_IP, PROVIDER);
        //then

        assertThatThrownBy(consent1Throwable)
                .isInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Error while retrieving consentId in " + PROVIDER + " missing scaOAuth");
        assertThatThrownBy(consent2Throwable)
                .isInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Error while retrieving consentId in " + PROVIDER + " missing consentId");
        assertThatThrownBy(consent3Throwable)
                .isInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Error while retrieving consentId in " + PROVIDER + " missing body");
    }

    @Test
    void shouldThrowExceptionWhenErrorIsThrownInCallOnGetConsent() throws TokenInvalidException {
        //given
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        doThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "statusText", HttpHeaders.EMPTY, null, null))
                .when(restTemplate).exchange(
                        eq("/berlingroup/v1/consents"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(BankVanBredaGroupConsent.class),
                        any(HttpErrorHandlerV2.class));
        TokenInvalidException thrownException = new TokenInvalidException("error message");
        doThrow(thrownException).when(errorHandler).handle(any(), any());

        //when
        ThrowableAssert.ThrowingCallable consentThrowable = () -> subject.getConsent(REDIRECT_URL, PSU_IP, PROVIDER);
        //then

        assertThatThrownBy(consentThrowable)
                .isInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Error while retrieving consentId in " + PROVIDER)
                .hasCause(thrownException);
    }

    @Test
    void shouldExecuteProperCallWhenCorrectDataIsProvidedOnDeleteConsent() throws TokenInvalidException {
        //given
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                nullable(HttpEntity.class),
                any(Class.class))
        ).thenReturn(ResponseEntity.noContent().build());

        //when
        subject.deleteConsent(CONSENT_ID);

        //then
        verify(restTemplate).exchange(
                eq("/berlingroup/v1/consents/" + CONSENT_ID),
                eq(HttpMethod.DELETE),
                isNull(),
                eq(Void.class));
    }

    @Test
    void shouldReturnProperBankLoginUrlWhenCorrectDataIsProvidedOnGetBankLoginUrl() {
        //given
        when(restTemplate.exchange(
                eq(SCA_OAUTH),
                eq(HttpMethod.GET),
                isNull(),
                eq(BankVanBredaGroupAuthorizationEndpoint.class))
        ).thenReturn(ResponseEntity.ok(new TestBankVanBredaGroupAuthorizationEndpoint("endpoint")));

        //when
        String loginUrl = subject.getBankLoginUrl(SCA_OAUTH, PROVIDER);
        //then

        assertThat(loginUrl).isEqualTo("endpoint");
    }

    @Test
    void shouldThrowExceptionWhenIncorrectBodyIsReceivedOnGetBankLoginUrl() {
        //given
        when(restTemplate.exchange(
                eq(SCA_OAUTH),
                eq(HttpMethod.GET),
                isNull(),
                eq(BankVanBredaGroupAuthorizationEndpoint.class))
        )
                .thenReturn(ResponseEntity.ok(new TestBankVanBredaGroupAuthorizationEndpoint(null)))
                .thenReturn(ResponseEntity.ok(null));

        //when
        ThrowableAssert.ThrowingCallable getBankLoginUrl1Throwable = () -> subject.getBankLoginUrl(SCA_OAUTH, PROVIDER);
        ThrowableAssert.ThrowingCallable getBankLoginUrl2Throwable = () -> subject.getBankLoginUrl(SCA_OAUTH, PROVIDER);
        //then

        assertThatThrownBy(getBankLoginUrl1Throwable)
                .isInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Error while retrieving authorization endpoint in " + PROVIDER + " authorizationUrl is empty");
        assertThatThrownBy(getBankLoginUrl2Throwable)
                .isInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Error while retrieving authorization endpoint in " + PROVIDER + " body is empty");
    }

    @Test
    void shouldThrowExceptionWhenErrorIsThrownInCallOnGetBankLoginUrl() throws TokenInvalidException {
        //given
        doThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "statusText", HttpHeaders.EMPTY, null, null))
                .when(restTemplate).exchange(
                        eq(SCA_OAUTH),
                        eq(HttpMethod.GET),
                        isNull(),
                        eq(BankVanBredaGroupAuthorizationEndpoint.class));
        TokenInvalidException thrownException = new TokenInvalidException("error message");
        doThrow(thrownException).when(errorHandler).handle(any(), any());
        //when
        ThrowableAssert.ThrowingCallable getBankLoginUrlThrowable = () -> subject.getBankLoginUrl(SCA_OAUTH, PROVIDER);
        //then

        assertThatThrownBy(getBankLoginUrlThrowable)
                .isInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Error while retrieving authorization endpoint in " + PROVIDER)
                .hasCause(thrownException);
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnGetUserToken() {
        //given
        TestBankVanBredaGroupAuthData expectedTokens = new TestBankVanBredaGroupAuthData(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN, SCOPE);
        when(restTemplate.exchange(
                eq("/berlingroup/v1/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BankVanBredaGroupAuthData.class))
        ).thenReturn(ResponseEntity.ok(expectedTokens));

        //when
        BankVanBredaGroupAuthData tokens = subject.getUserToken(AUTH_CODE, TPP_ID, CODE_VERIFIER, BASE_URL, PROVIDER);
        //then

        assertThat(toJson(expectedTokens)).isEqualTo(toJson(tokens));
    }

    @Test
    void shouldThrowExceptionWhenIncorrectBodyIsReceivedOnGetUserToken() {
        //given
        when(restTemplate.exchange(
                eq("/berlingroup/v1/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BankVanBredaGroupAuthData.class))
        ).thenReturn(ResponseEntity.ok(null));

        //when
        ThrowableAssert.ThrowingCallable tokensCallable = () -> subject.getUserToken(AUTH_CODE, TPP_ID, CODE_VERIFIER, BASE_URL, PROVIDER);
        //then

        assertThatThrownBy(tokensCallable)
                .isInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Empty body with token response in " + PROVIDER);
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnRefreshUserToken() throws TokenInvalidException {
        TestBankVanBredaGroupAuthData expectedTokens = new TestBankVanBredaGroupAuthData(REFRESHED_ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN, SCOPE);
        when(restTemplate.exchange(
                eq("/berlingroup/v1/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BankVanBredaGroupAuthData.class))
        ).thenReturn(ResponseEntity.ok(expectedTokens));
        //when
        BankVanBredaGroupAuthData tokens = subject.refreshUserToken(REFRESH_TOKEN, TPP_ID, PROVIDER);
        //then
        assertThat(toJson(expectedTokens)).isEqualTo(toJson(tokens));
    }

    @Test
    void shouldThrowExceptionWhenIncorrectBodyIsReceivedOnRefreshUserToken() {
        when(restTemplate.exchange(
                eq("/berlingroup/v1/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BankVanBredaGroupAuthData.class))
        ).thenReturn(ResponseEntity.ok(null));
        //when
        ThrowableAssert.ThrowingCallable tokensCallable = () -> subject.refreshUserToken(REFRESH_TOKEN, TPP_ID, PROVIDER);
        //then
        assertThatThrownBy(tokensCallable)
                .isInstanceOf(TokenInvalidException.class)
                .hasMessage("Empty body with token response in " + PROVIDER);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Problem when writing " + object + " as string");
        }
    }
}
