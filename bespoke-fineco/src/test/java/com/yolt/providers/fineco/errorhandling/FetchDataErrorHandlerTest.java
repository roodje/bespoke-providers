package com.yolt.providers.fineco.errorhandling;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class FetchDataErrorHandlerTest {

    private static final String TEST_PSU_IP_ADDRESS = "12.34.56.78";
    private static final String NOT_FOUND_EXCEPTION_TO_SWALLOW_TEXT = "Can't find accounts belonging to the specific consentId";
    private static final String RESOURCE_UNKNOWN_CODE = "RESOURCE_UNKNOWN";

    @Test
    void shouldRethrowNotFoundExceptionOnHttp400WhenNotFoundExceptionToSwallowTextIsNotPresent() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.NOT_FOUND,
                "404",
                new HttpHeaders(),
                "Random text".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, TEST_PSU_IP_ADDRESS))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void shouldSwallowNotFoundExceptionOnHttp400WhenNotFoundExceptionToSwallowTextIsPresent() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.NOT_FOUND,
                "404",
                new HttpHeaders(),
                NOT_FOUND_EXCEPTION_TO_SWALLOW_TEXT.getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatCode(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, TEST_PSU_IP_ADDRESS))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowTokenInvalidExceptionOnHttp401Unauthorized() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                "401",
                new HttpHeaders(),
                "Authorization required".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, TEST_PSU_IP_ADDRESS))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionOnHttp403Forbidden() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                "401",
                new HttpHeaders(),
                "You don't have permission".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, TEST_PSU_IP_ADDRESS))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldThrowProviderFetchDataExceptionOnHttp500InternalServerError() {
        // given
        HttpServerErrorException exception = HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                "500",
                new HttpHeaders(),
                "Something went wrong".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, TEST_PSU_IP_ADDRESS))
                .isInstanceOf(ProviderFetchDataException.class);
    }

    @Test
    void shouldThrowBackPressureExceptionOnHttp429TooManyRequestsWhenPsuIpAddressIsNotPresent() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS,
                "429",
                new HttpHeaders(),
                "Too many requests".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, null))
                .isInstanceOf(BackPressureRequestException.class);
    }

    @Test
    void shouldThrowProviderFetchDataExceptionOnHttp429TooManyRequestWhenPsuIpAddressIsPresent() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.NotFound.create(HttpStatus.TOO_MANY_REQUESTS,
                "429",
                new HttpHeaders(),
                "Too many requests".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, TEST_PSU_IP_ADDRESS))
                .isInstanceOf(ProviderFetchDataException.class);
    }

    @Test
    void shouldThrowProviderFetchDataExceptionOnRandomException() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.I_AM_A_TEAPOT,
                "418",
                new HttpHeaders(),
                "I'm a teapot".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, TEST_PSU_IP_ADDRESS))
                .isInstanceOf(ProviderFetchDataException.class);
    }

    @Test
    void shouldThrowProviderFetchDataExceptionOnHttp400BadRequestWhenResourceUnknownCodeIsNotPresent() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
                "400",
                new HttpHeaders(),
                "Random message".getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, TEST_PSU_IP_ADDRESS))
                .isInstanceOf(ProviderFetchDataException.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionOnHttp400BadRequestWhenResourceUnknownCodeIsPresent() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
                "400",
                new HttpHeaders(),
                RESOURCE_UNKNOWN_CODE.getBytes(StandardCharsets.UTF_8),
                Charset.defaultCharset());

        // when
        assertThatThrownBy(() -> FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(exception, TEST_PSU_IP_ADDRESS))
                .isInstanceOf(TokenInvalidException.class);
    }

}