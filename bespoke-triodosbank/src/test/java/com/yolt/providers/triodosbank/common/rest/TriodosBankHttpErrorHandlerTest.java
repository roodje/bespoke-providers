package com.yolt.providers.triodosbank.common.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.triodosbank.common.exception.ProviderHttpStatusException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriodosBankHttpErrorHandlerTest {

    private final TriodosBankHttpErrorHandler httpErrorHandler = new TriodosBankHttpErrorHandler();

    @Test
    void shouldThrowProviderHttpStatusExceptionOnRegularHttp400() {
        // given
        HttpStatusCodeException httpStatusCodeException = HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
                "400 - Bad Request",
                new HttpHeaders(),
                "Malformed request".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> httpErrorHandler.handleNon2xxResponseCode(httpStatusCodeException))
                .isExactlyInstanceOf(ProviderHttpStatusException.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionOnHttp400IndicatingUnrecognizedToken() {
        // given
        HttpStatusCodeException httpStatusCodeException = HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
                "400 - Bad Request",
                new HttpHeaders(),
                "{\"error\":\"invalid_request\",\"error_description\":\"Unknown refresh token\"}".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> httpErrorHandler.handleNon2xxResponseCode(httpStatusCodeException))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionOnHttp400IndicatingUnauthorizedClient() {
        // given
        HttpStatusCodeException httpStatusCodeException = HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
                "400 - Bad Request",
                new HttpHeaders(),
                "{\"error\":\"unauthorized_client\",\"error_description\":\"Authorization code not issued to client: PSDNL-DNB-R179712\"}"
                        .getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> httpErrorHandler.handleNon2xxResponseCode(httpStatusCodeException))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionOnHttp401() {
        // given
        HttpStatusCodeException httpStatusCodeException = HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                "401 - Unauthorized",
                new HttpHeaders(),
                "Authorization required".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> httpErrorHandler.handleNon2xxResponseCode(httpStatusCodeException))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionOnHttp403() {
        // given
        HttpStatusCodeException httpStatusCodeException = HttpClientErrorException.create(HttpStatus.FORBIDDEN,
                "403 - Forbidden",
                new HttpHeaders(),
                "No permission".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> httpErrorHandler.handleNon2xxResponseCode(httpStatusCodeException))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldThrowProviderHttpStatusExceptionOnHttp500() {
        // given
        HttpStatusCodeException httpStatusCodeException = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                "500 - Internal Server Error",
                new HttpHeaders(),
                "Something went wrong".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> httpErrorHandler.handleNon2xxResponseCode(httpStatusCodeException))
                .isExactlyInstanceOf(ProviderHttpStatusException.class);
    }

    @Test
    void shouldThrowProviderHttpStatusExceptionOnHttp429() {
        // given
        HttpStatusCodeException httpStatusCodeException = HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS,
                "429 - Too many requests",
                new HttpHeaders(),
                "Too many requests".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> httpErrorHandler.handleNon2xxResponseCode(httpStatusCodeException))
                .isExactlyInstanceOf(ProviderHttpStatusException.class);
    }
}