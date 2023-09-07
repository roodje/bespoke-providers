package com.yolt.providers.raiffeisenbank.common;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.Consent;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.RaiffeisenAuthData;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.TestConsent;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.TestRaiffeisenAuthData;
import com.yolt.providers.raiffeisenbank.common.ais.config.RaiffeisenBankProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static com.yolt.providers.raiffeisenbank.common.RaiffeisenBankSampleAuthenticationMeans.CLIENT_ID_SAMPLE;
import static com.yolt.providers.raiffeisenbank.common.RaiffeisenBankSampleAuthenticationMeans.CLIENT_SECRET_SAMPLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaiffeisenBankHttpClientTest {

    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String REDIRECT_URL = "redirectUrl";
    private static final String PSU_IP = "psuIp";
    private static final String IBAN = "iban";
    private static final String CONSENT_ID = "consentId";
    private static final String PROVIDER = "provider";
    private static final String BASE_OAUTH_URL = "https://www.baseOauthUrl.com";
    private static final long EXPIRES_IN = 10L;
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESHED_ACCESS_TOKEN = "refreshed_access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String REFRESHED_REFRESH_TOKEN = "refresh_token";
    private static final String AUTH_CODE = "authCode";

    @Mock
    private RaiffeisenBankProperties properties;
    @Mock
    private HttpErrorHandlerV2 errorHandler;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Clock clock;

    private RaiffeisenBankHttpClient subject;

    @BeforeEach
    public void setup() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
        subject = new RaiffeisenBankHttpClient(properties, errorHandler, meterRegistry, restTemplate, clock, PROVIDER);
    }

    @Test
    void shouldReturnProperConsentIdWhenCorrectDataIsProvidedOnCreateConsentId() throws TokenInvalidException {
        //given
        when(restTemplate.exchange(
                eq("/psd2-bgs-consent-api-1.3.2-rbro/v1/consents"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Consent.class))
        ).thenReturn(ResponseEntity.ok(new TestConsent(CONSENT_ID)));
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        //when
        var consentId = subject.createConsentId(CLIENT_ID, REDIRECT_URL, PSU_IP, IBAN, ACCESS_TOKEN);
        //then
        assertThat(consentId).isPresent();
        assertThat(consentId.get()).isEqualTo(CONSENT_ID);
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnGetUserToken() throws TokenInvalidException {
        //given
        when(restTemplate.exchange(
                eq(BASE_OAUTH_URL + "/aisp/oauth2/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(RaiffeisenAuthData.class))
        ).thenReturn(ResponseEntity.ok(new TestRaiffeisenAuthData(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN)));
        when(properties.getOAuthBaseUrl()).thenReturn(BASE_OAUTH_URL);
        //when
        RaiffeisenAuthData tokens = subject.getUserToken(CLIENT_ID, CLIENT_SECRET, AUTH_CODE, REDIRECT_URL).get();
        //then
        assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(tokens.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(tokens.getExpiresIn()).isEqualTo(EXPIRES_IN);
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnRefreshUserToken() throws TokenInvalidException {
        when(restTemplate.exchange(
                eq(BASE_OAUTH_URL + "/aisp/oauth2/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(RaiffeisenAuthData.class))
        ).thenReturn(ResponseEntity.ok(new TestRaiffeisenAuthData(REFRESHED_ACCESS_TOKEN, EXPIRES_IN, REFRESHED_REFRESH_TOKEN)));
        when(properties.getOAuthBaseUrl()).thenReturn(BASE_OAUTH_URL);
        //when
        RaiffeisenAuthData tokens = subject.refreshUserToken(CLIENT_ID_SAMPLE.toString(), CLIENT_SECRET_SAMPLE, REFRESH_TOKEN).get();
        //then
        assertThat(tokens.getAccessToken()).isEqualTo(REFRESHED_ACCESS_TOKEN);
        assertThat(tokens.getRefreshToken()).isEqualTo(REFRESHED_REFRESH_TOKEN);
        assertThat(tokens.getExpiresIn()).isEqualTo(EXPIRES_IN);
    }

    @Test
    void shouldProperlyCallEndpointWhenCorrectDataIsProvidedOnDeleteConsent() throws TokenInvalidException {
        when(restTemplate.exchange(
                eq("/psd2-bgs-consent-api-1.3.2-rbro/v1/consents/" + CONSENT_ID),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.noContent().build());
        //when
        subject.deleteConsent(CONSENT_ID, ACCESS_TOKEN, CLIENT_ID);
        //then
        verify(restTemplate).exchange(
                eq("/psd2-bgs-consent-api-1.3.2-rbro/v1/consents/" + CONSENT_ID),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }
}
