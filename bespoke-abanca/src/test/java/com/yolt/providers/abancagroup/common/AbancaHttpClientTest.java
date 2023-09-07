package com.yolt.providers.abancagroup.common;

import com.yolt.providers.abancagroup.common.ais.auth.dto.AbancaAuthData;
import com.yolt.providers.abancagroup.common.ais.auth.dto.TestAbancaAuthData;
import com.yolt.providers.abancagroup.common.ais.config.AbancaGroupProperties;
import com.yolt.providers.abancagroup.common.ais.data.service.AbancaSigningService;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbancaHttpClientTest {

    private static final UUID CLIENT_ID = UUID.randomUUID();
    private static final UUID API_KEY = UUID.randomUUID();
    private static final String PROVIDER = "provider";
    private static final long EXPIRES_IN = 10L;
    private static final String ACCESS_TOKEN = "access_token";
    private static final String BASE_URL = "https://www.baseUrl.com";
    private static final String REFRESHED_ACCESS_TOKEN = "refreshed_access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String REFRESHED_REFRESH_TOKEN = "refresh_token";
    private static final String AUTH_CODE = "authCode";

    @Mock
    private AbancaGroupProperties properties;
    @Mock
    private HttpErrorHandlerV2 errorHandler;
    @Mock
    private AbancaSigningService signingService;
    @Mock
    private RestTemplate restTemplate;

    private AbancaHttpClient subject;

    @BeforeEach
    public void setup() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
        subject = new AbancaHttpClient(signingService, properties, errorHandler, meterRegistry, restTemplate, PROVIDER);
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnGetUserToken() throws TokenInvalidException {
        //given
        when(restTemplate.exchange(
                eq(BASE_URL + "/oauth2/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AbancaAuthData.class))
        ).thenReturn(ResponseEntity.ok(new TestAbancaAuthData(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN)));
        when(properties.getBaseUrl()).thenReturn(BASE_URL);
        //when
        AbancaAuthData tokens = subject.getUserToken(CLIENT_ID, API_KEY, AUTH_CODE).get();
        //then
        assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(tokens.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(tokens.getExpiresIn()).isEqualTo(EXPIRES_IN);
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnRefreshUserToken() throws TokenInvalidException {
        when(restTemplate.exchange(
                eq(BASE_URL + "/oauth2/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AbancaAuthData.class))
        ).thenReturn(ResponseEntity.ok(new TestAbancaAuthData(REFRESHED_ACCESS_TOKEN, EXPIRES_IN, REFRESHED_REFRESH_TOKEN)));
        when(properties.getBaseUrl()).thenReturn(BASE_URL);
        //when
        AbancaAuthData tokens = subject.refreshUserToken(CLIENT_ID, API_KEY, REFRESH_TOKEN).get();
        //then
        assertThat(tokens.getAccessToken()).isEqualTo(REFRESHED_ACCESS_TOKEN);
        assertThat(tokens.getRefreshToken()).isEqualTo(REFRESHED_REFRESH_TOKEN);
        assertThat(tokens.getExpiresIn()).isEqualTo(EXPIRES_IN);
    }
}
