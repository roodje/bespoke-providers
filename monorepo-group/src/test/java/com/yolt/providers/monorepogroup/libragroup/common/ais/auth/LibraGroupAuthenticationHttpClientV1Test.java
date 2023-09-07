package com.yolt.providers.monorepogroup.libragroup.common.ais.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans.SigningData;
import com.yolt.providers.monorepogroup.libragroup.common.LibraSigningService;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.*;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraGroupAuthenticationHttpClientV1Test {

    private static final String PROVIDER = "provider";
    private static final long EXPIRES_IN = 10L;
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESHED_ACCESS_TOKEN = "refreshed_access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String AUTH_CODE = "authCode";
    private static final String CLIENT_ID = "tppId";
    private static final String CONSENT_ID = "consentId";
    private static final String SIGNING_KEY_SERIAL_NUMBER = "serialNumber";
    private static final UUID SIGNING_KEY_ID = UUID.randomUUID();
    private static final String SIGNING_CERTIFICATE = "signingCertificate";
    private static final SigningData SIGNING_DATA = new SigningData(
            SIGNING_CERTIFICATE,
            SIGNING_KEY_SERIAL_NUMBER,
            SIGNING_KEY_ID
    );
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String REDIRECT_URL = "redirectUrl";

    @Mock
    private HttpErrorHandlerV2 errorHandler;
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LibraSigningService signingService;
    @Mock
    private Signer signer;
    @Captor
    private ArgumentCaptor<HttpEntity<LibraGroupConsentRequest>> httpEntityCaptor;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));

    private LibraGroupAuthenticationHttpClientV1 subject;

    @BeforeEach
    public void setup() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
        subject = new LibraGroupAuthenticationHttpClientV1(signingService, errorHandler, meterRegistry, restTemplate, CLOCK, PROVIDER);
    }

    @Test
    void shouldReturnProperConsentWhenCorrectDataIsProvidedOnGetConsent() throws TokenInvalidException {
        //given
        when(signingService.getSigningHeaders(
                any(LibraGroupConsentRequest.class),
                eq(SIGNING_KEY_SERIAL_NUMBER),
                eq(SIGNING_KEY_ID),
                eq(SIGNING_CERTIFICATE),
                eq(signer)))
                .thenReturn(new HttpHeaders());
        when(restTemplate.exchange(
                eq("/CONSENTS_API/1.0"),
                eq(HttpMethod.POST),
                httpEntityCaptor.capture(),
                eq(LibraGroupConsent.class))
        ).thenReturn(ResponseEntity.ok(new TestLibraGroupConsent(CONSENT_ID)));

        //when
        LibraGroupConsent consent = subject.getConsent(
                SIGNING_DATA,
                ACCESS_TOKEN,
                signer,
                PROVIDER);
        //then

        assertThat(toJson(httpEntityCaptor.getValue().getBody())).isEqualTo(toJson(new LibraGroupConsentRequest(CLOCK)));
        assertThat(toJson(consent)).isEqualTo(toJson(new TestLibraGroupConsent(CONSENT_ID)));
    }

    @Test
    void shouldThrowProperExceptionWhenIncorrectBodyIsReceivedOnGetConsent() throws TokenInvalidException {
        //given
        when(signingService.getSigningHeaders(
                any(LibraGroupConsentRequest.class),
                eq(SIGNING_KEY_SERIAL_NUMBER),
                eq(SIGNING_KEY_ID),
                eq(SIGNING_CERTIFICATE),
                eq(signer)))
                .thenReturn(new HttpHeaders());
        when(restTemplate.exchange(
                eq("/CONSENTS_API/1.0"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LibraGroupConsent.class))
        )
                .thenReturn(ResponseEntity.ok(new TestLibraGroupConsent(null)))
                .thenReturn(ResponseEntity.ok(null));

        //when
        ThrowableAssert.ThrowingCallable consent1Throwable = () -> subject.getConsent(
                SIGNING_DATA,
                ACCESS_TOKEN,
                signer,
                PROVIDER);
        ThrowableAssert.ThrowingCallable consent2Throwable = () -> subject.getConsent(
                SIGNING_DATA,
                ACCESS_TOKEN,
                signer,
                PROVIDER);
        //then

        assertThatThrownBy(consent1Throwable)
                .isInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Error while retrieving consentId in " + PROVIDER + " missing consentId");
        assertThatThrownBy(consent2Throwable)
                .isInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Error while retrieving consentId in " + PROVIDER + " missing body");
    }

    @Test
    void shouldThrowProperExceptionWhenErrorIsThrownInCallOnGetConsent() throws TokenInvalidException {
        //given
        when(signingService.getSigningHeaders(
                any(LibraGroupConsentRequest.class),
                eq(SIGNING_KEY_SERIAL_NUMBER),
                eq(SIGNING_KEY_ID),
                eq(SIGNING_CERTIFICATE),
                eq(signer)))
                .thenReturn(new HttpHeaders());
        doThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "statusText", HttpHeaders.EMPTY, null, null))
                .when(restTemplate).exchange(
                        eq("/CONSENTS_API/1.0"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(LibraGroupConsent.class),
                        any(HttpErrorHandlerV2.class));
        TokenInvalidException thrownException = new TokenInvalidException("error message");
        doThrow(thrownException).when(errorHandler).handle(any(), any());

        //when
        ThrowableAssert.ThrowingCallable consentThrowable = () -> subject.getConsent(
                SIGNING_DATA,
                ACCESS_TOKEN,
                signer,
                PROVIDER);
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
                any(HttpEntity.class),
                any(Class.class))
        ).thenReturn(ResponseEntity.noContent().build());
        when(signingService.getSigningHeaders(
                new LinkedMultiValueMap<>(),
                SIGNING_KEY_SERIAL_NUMBER,
                SIGNING_KEY_ID,
                SIGNING_CERTIFICATE,
                signer)
        ).thenReturn(new HttpHeaders());
        //when
        subject.deleteConsent(CONSENT_ID, SIGNING_DATA, signer);

        //then
        verify(restTemplate).exchange(
                eq("/CONSENTS_API/1.0/" + CONSENT_ID),
                eq(HttpMethod.DELETE),
                any(),
                eq(Void.class));
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnGetUserToken() {
        //given
        TestLibraGroupAuthData expectedTokens = new TestLibraGroupAuthData(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN);
        when(restTemplate.exchange(
                eq("/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LibraGroupAuthData.class))
        ).thenReturn(ResponseEntity.ok(expectedTokens));

        //when
        LibraGroupAuthData tokens = subject.getUserToken(
                CLIENT_ID,
                CLIENT_SECRET,
                AUTH_CODE,
                REDIRECT_URL,
                CONSENT_ID,
                PROVIDER);
        //then

        assertThat(toJson(expectedTokens)).isEqualTo(toJson(tokens));
    }

    @Test
    void shouldThrowProperExceptionWhenIncorrectBodyIsReceivedOnGetUserToken() {
        //given
        when(restTemplate.exchange(
                eq("/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LibraGroupAuthData.class))
        ).thenReturn(ResponseEntity.ok(null));

        //when
        ThrowableAssert.ThrowingCallable tokensCallable = () -> subject.getUserToken(
                CLIENT_ID,
                CLIENT_SECRET,
                AUTH_CODE,
                REDIRECT_URL,
                CONSENT_ID,
                PROVIDER);

        //then

        assertThatThrownBy(tokensCallable)
                .isInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Empty body with token response in " + PROVIDER);
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnRefreshUserToken() throws TokenInvalidException {
        TestLibraGroupAuthData expectedTokens = new TestLibraGroupAuthData(REFRESHED_ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN);
        when(restTemplate.exchange(
                eq("/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LibraGroupAuthData.class))
        ).thenReturn(ResponseEntity.ok(expectedTokens));
        //when
        LibraGroupAuthData tokens = subject.refreshUserToken(
                CLIENT_ID,
                CLIENT_SECRET,
                REFRESH_TOKEN,
                REDIRECT_URL,
                PROVIDER);
        //then
        assertThat(toJson(expectedTokens)).isEqualTo(toJson(tokens));
    }

    @Test
    void shouldThrowProperExceptionWhenIncorrectBodyIsReceivedOnRefreshUserToken() {
        when(restTemplate.exchange(
                eq("/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LibraGroupAuthData.class))
        ).thenReturn(ResponseEntity.ok(null));

        //when
        ThrowableAssert.ThrowingCallable tokensCallable = () -> subject.refreshUserToken(
                CLIENT_ID,
                CLIENT_SECRET,
                REFRESH_TOKEN,
                REDIRECT_URL,
                PROVIDER);
        //then
        assertThatThrownBy(tokensCallable)
                .isInstanceOf(TokenInvalidException.class)
                .hasMessage("Empty body with token response in " + PROVIDER);
    }

    private String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Problem when writing " + object + " as string");
        }
    }
}
