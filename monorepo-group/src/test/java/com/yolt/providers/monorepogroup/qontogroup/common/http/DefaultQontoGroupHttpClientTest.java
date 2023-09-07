package com.yolt.providers.monorepogroup.qontogroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Organization;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.TokenResponse;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transactions;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.QontoGroupDateMapper;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
class DefaultQontoGroupHttpClientTest {

    private MeterRegistry registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());

    private String tokenUrl = "https://tokenurl.com/token";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Signer signer;

    private Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));

    private HttpErrorHandlerV2 errorHandlerV2 = new DefaultHttpErrorHandlerV3();
    private QontoGroupDateMapper dateMapper = new QontoGroupDateMapper(ZoneId.of("Europe/Paris"), clock);

    @Captor
    private ArgumentCaptor<HttpEntity<LinkedMultiValueMap<String, String>>> httpEntityArgumentCaptor;

    private DefaultQontoGroupHttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = new DefaultQontoGroupHttpClient(registry, restTemplate, "PROVIDER", errorHandlerV2, tokenUrl, dateMapper);
    }

    @Test
    void shouldCreateTokens() throws TokenInvalidException {
        //given
        var tokenRequest = new LinkedMultiValueMap<String, String>();
        var tokenResponse = mock(TokenResponse.class);
        given(restTemplate.exchange(eq(tokenUrl),
                eq(HttpMethod.POST),
                httpEntityArgumentCaptor.capture(),
                eq(TokenResponse.class))).willReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        //when
        var receivedResponse = httpClient.createToken(tokenRequest);

        //then
        assertThat(httpEntityArgumentCaptor.getValue().getBody()).isEqualTo(tokenRequest);
        assertThat(receivedResponse).isEqualTo(tokenResponse);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenHttp401IsReceivedFromBank() throws TokenInvalidException {
        //given
        var tokenRequest = new LinkedMultiValueMap<String, String>();
        var expectedException = new HttpServerErrorException(HttpStatus.UNAUTHORIZED);
        given(restTemplate.exchange(eq(tokenUrl),
                eq(HttpMethod.POST),
                httpEntityArgumentCaptor.capture(),
                eq(TokenResponse.class))).willThrow(expectedException);

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpClient.createToken(tokenRequest);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("We are not authorized to call endpoint: HTTP 401")
                .withCause(expectedException);
    }

    @Test
    void shouldFetchOrganization() throws TokenInvalidException {
        var accessToken = "accessToken";
        var psuIpAddress = "psuIpAddress";
        var signingData = new QontoGroupAuthenticationMeans.SigningData("url", UUID.randomUUID());
        given(signer.sign(any(byte[].class), any(), any())).willReturn("signature string");
        var expectedResponse = mock(Organization.class);
        given(restTemplate.exchange(eq("/v2/organization"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Organization.class))).willReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        //when
        var result = httpClient.fetchOrganization(accessToken, psuIpAddress, signer, signingData);

        //then
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void shouldFetchTransactions() throws TokenInvalidException {
        Instant startFetchTime = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        var transactionStartFetchDateTime = LocalDateTime.ofInstant(startFetchTime, ZoneId.of("UTC")).format(formatter);
        var accessToken = "accessToken";
        var psuIpAddress = "psuIpAddress";
        var signingData = new QontoGroupAuthenticationMeans.SigningData("url", UUID.randomUUID());
        given(signer.sign(any(byte[].class), any(), any())).willReturn("signature string");
        var expectedResponse = mock(Transactions.class);
        given(restTemplate.exchange(eq("/v2/transactions?iban={iban}&emitted_at_from={dateFrom}&current_page={pageNumber}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Transactions.class),
                eq("IBAN1"),
                eq(transactionStartFetchDateTime),
                eq("1"))).willReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        //when
        var result = httpClient.fetchTransactions(accessToken, psuIpAddress, signer, signingData, "IBAN1", startFetchTime, "1");

        //then
        assertThat(result).isEqualTo(expectedResponse);
    }
}