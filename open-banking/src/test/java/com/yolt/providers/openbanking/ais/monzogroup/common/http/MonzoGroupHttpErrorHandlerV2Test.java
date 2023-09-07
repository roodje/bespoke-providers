package com.yolt.providers.openbanking.ais.monzogroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.charset.Charset;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class MonzoGroupHttpErrorHandlerV2Test {

    private final MonzoGroupHttpErrorHandlerV2 handler = new MonzoGroupHttpErrorHandlerV2();

    @Test
    void shouldThrowInvalidTokenExceptionOn403() {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldThrowInvalidTokenExceptionOn401() {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldRethrowExceptionOn400WithoutProperMessage() {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "BAD REQUEST", "Something else went wrong".getBytes(), Charset.defaultCharset()));

        // then
        assertThatThrownBy(errorHandler).isInstanceOf(HttpStatusCodeException.class);
    }

    @ParameterizedTest
    @MethodSource("getHttp400BadRequestBodiesThatShouldResultInTokenInvalidException")
    void shouldThrowTokenInvalidExceptionOnHttp400WithSpecificBodyContent(String body) {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "BAD REQUEST",
                body.getBytes(),
                Charset.defaultCharset()));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    private static Stream<String> getHttp400BadRequestBodiesThatShouldResultInTokenInvalidException() {
        return Stream.of(
                "{\"error\":\"invalid_request\",\"error_description\":\"Refresh token has been evicted\",\"error_params\":{\"original_error_code\":\"bad_request.refresh_token.evicted\",\"refresh_token_id\":\"<REFRESH_TOKEN_ID>\"}}",
                "{\"code\":\"bad_request.client_mismatch\",\"message\":\"Client does not belong to same org as client certificate\",\"params\":{\"client_cert_org_id\":\"0014H00002LmnTjQAJ\",\"client_id\":\"oauth2client_CONFIDENTIAL\",\"client_org_id\":\"001580000103UAYAA2\"},\"retryable\":{}}",
                "{\"error\":\"invalid_request\",\"error_description\":\"Bad refresh token\",\"error_params\":{\"jwt_error\":\"Version not supported\",\"original_error_code\":\"bad_request.bad_refresh_token\"}}",
                "{\"error\":\"invalid_request\",\"error_description\":\"Refresh token is not valid\",\"error_params\":{\"original_error_code\":\"bad_request.refresh_token.invalid\",\"refresh_token_id\":\"reftok_1237\"}}",
                "{\"error\":\"invalid_request\",\"error_description\":\"invalid_grant\",\"error_params\":{\"original_error_code\":\"bad_request.invalid_grant\",\"refresh_token_id\":\"reftok_1236\"}}",
                "{\"error\":\"invalid_request\",\"error_description\":\"Refresh token has been invalidated\",\"error_params\":{\"original_error_code\":\"bad_request.refresh_token.invalid\",\"refresh_token_id\":\"reftok_1235\"}}",
                "{\"error\":\"invalid_request\",\"error_description\":\"Refresh token has expired\",\"error_params\":{\"original_error_code\":\"bad_request.refresh_token.expired\",\"refresh_token_id\":\"reftok_1234\"}}"
        );
    }
}
