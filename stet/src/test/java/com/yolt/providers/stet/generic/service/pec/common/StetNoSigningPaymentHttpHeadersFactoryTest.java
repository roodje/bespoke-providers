package com.yolt.providers.stet.generic.service.pec.common;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.HttpHeadersExtension;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class StetNoSigningPaymentHttpHeadersFactoryTest {

    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String BASIC_AUTHORIZATION = "Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=";
    private static final String ACCESS_TOKEN = "c0cc8fdf-1753-4604-a2c7-a4d11d642a0c";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String LAST_EXTERNAL_TRACE_ID = "5e5421a6-1de8-4974-965d-410750e7d180";

    private StetNoSigningPaymentHttpHeadersFactory httpHeadersFactory;

    @BeforeEach
    void setUp() {
        httpHeadersFactory = new StetNoSigningPaymentHttpHeadersFactory(() -> LAST_EXTERNAL_TRACE_ID);
    }

    @Test
    void shouldCreateHttpHeadersForPaymentAccessToken() {
        // given
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        StetTokenPaymentPreExecutionResult preExecutionResult = StetTokenPaymentPreExecutionResult.builder()
                .authMeans(DefaultAuthenticationMeans.builder()
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .build())
                .build();

        // when
        HttpHeaders httpHeaders = httpHeadersFactory.createPaymentAccessTokenHttpHeaders(preExecutionResult, requestBody);

        // then
        assertThat(httpHeaders.toSingleValueMap())
                .hasSize(3)
                .containsEntry(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .containsEntry(HttpHeaders.AUTHORIZATION, BASIC_AUTHORIZATION)
                .containsEntry(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    @Test
    void shouldCreateHttpHeadersForPaymentInitiation() {
        // given
        StetPaymentInitiationRequestDTO requestDTO = StetPaymentInitiationRequestDTO.builder()
                .build();

        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .accessToken(ACCESS_TOKEN)
                .psuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        HttpHeaders httpHeaders = httpHeadersFactory.createPaymentInitiationHttpHeaders(preExecutionResult, requestDTO);

        // then
        assertThat(httpHeaders.toSingleValueMap()).satisfies(validateHttpHeaders());
    }

    @Test
    void shouldCreateHttpHeadersForPaymentSubmit() {
        // given
        StetPaymentConfirmationRequestDTO requestDTO = StetPaymentConfirmationRequestDTO.builder()
                .build();

        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .accessToken(ACCESS_TOKEN)
                .psuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        HttpHeaders httpHeaders = httpHeadersFactory.createPaymentSubmitHttpHeaders(preExecutionResult, requestDTO);

        // then
        assertThat(httpHeaders.toSingleValueMap()).satisfies(validateHttpHeaders());
    }

    @Test
    void shouldCreateHttpHeadersForPaymentStatus() {
        // given
        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .accessToken(ACCESS_TOKEN)
                .psuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        HttpHeaders httpHeaders = httpHeadersFactory.createPaymentStatusHttpHeaders(preExecutionResult);

        // then
        assertThat(httpHeaders.toSingleValueMap()).satisfies(validateHttpHeaders());
    }

    @Test
    void shouldPrepareCommonHttpHeaders() {
        // when
        HttpHeaders httpHeaders = httpHeadersFactory.prepareCommonHttpHeaders(ACCESS_TOKEN, PSU_IP_ADDRESS);

        // then
        assertThat(httpHeaders.toSingleValueMap()).satisfies(validateHttpHeaders());
    }

    private Consumer<Map<String, String>> validateHttpHeaders() {
        return (httpHeaders) -> {
            assertThat(httpHeaders)
                    .hasSize(5)
                    .containsEntry(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .containsEntry(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                    .containsEntry(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .containsEntry(HttpHeadersExtension.PSU_IP_ADDRESS, PSU_IP_ADDRESS)
                    .containsEntry(HttpHeadersExtension.X_REQUEST_ID, LAST_EXTERNAL_TRACE_ID);
        };
    }
}
