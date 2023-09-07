package com.yolt.providers.stet.generic.http.client;

import com.yolt.providers.common.rest.http.DefaultHttpErrorHandler;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Deprecated
class NoErrorHandlingHttpClientTest {

    private static final String ENDPOINT = "/example";
    private static final String PROMETHEUS_PATH = "example_path";
    private static final HttpMethod HTTP_METHOD = HttpMethod.POST;

    @Mock
    private RestTemplate restTemplate;

    private NoErrorHandlingHttpClient httpClient;

    @BeforeEach
    void initialize() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        httpClient = new NoErrorHandlingHttpClient(meterRegistry, restTemplate, "Example Provider");
    }

    @ParameterizedTest
    @CsvSource({"BAD_REQUEST", "UNAUTHORIZED", "FORBIDDEN", "INTERNAL_SERVER_ERROR"})
    void shouldNotHandleHttpStatusCodeExceptionDuringExchange(String inputHttpStatus) {
        // given
        HttpStatus httpStatus = HttpStatus.valueOf(inputHttpStatus);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class), any(String.class)))
                .thenThrow(new HttpClientErrorException(httpStatus));

        // when
        ThrowingCallable throwingCallable = () ->
                httpClient.exchange(ENDPOINT, HTTP_METHOD, HttpEntity.EMPTY, PROMETHEUS_PATH, Void.class);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(HttpStatusCodeException.class);
    }

    @ParameterizedTest
    @CsvSource({"BAD_REQUEST", "UNAUTHORIZED", "FORBIDDEN", "INTERNAL_SERVER_ERROR"})
    void shouldNotHandleHttpStatusCodeExceptionDuringExchangeEvenIfHttpErrorHandlerIsProvided(String inputHttpStatus) {
        // given
        HttpStatus httpStatus = HttpStatus.valueOf(inputHttpStatus);
        HttpErrorHandler defaultHttpErrorHandler = new DefaultHttpErrorHandler();

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class), any(String.class)))
                .thenThrow(new HttpClientErrorException(httpStatus));

        // when
        ThrowingCallable throwingCallable = () ->
                httpClient.exchange(ENDPOINT, HTTP_METHOD, HttpEntity.EMPTY, PROMETHEUS_PATH, Void.class, defaultHttpErrorHandler);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(HttpStatusCodeException.class);
    }

    @ParameterizedTest
    @CsvSource({"BAD_REQUEST", "UNAUTHORIZED", "FORBIDDEN", "INTERNAL_SERVER_ERROR"})
    void shouldNotHandleHttpStatusCodeExceptionDuringExchangeForBody(String inputHttpStatus) {
        // given
        HttpStatus httpStatus = HttpStatus.valueOf(inputHttpStatus);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class), any(String.class)))
                .thenThrow(new HttpClientErrorException(httpStatus));

        // when
        ThrowingCallable throwingCallable = () ->
                httpClient.exchangeForBody(ENDPOINT, HTTP_METHOD, HttpEntity.EMPTY, PROMETHEUS_PATH, Void.class);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(HttpStatusCodeException.class);
    }
}
