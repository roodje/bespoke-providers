package com.yolt.providers.stet.generic.service.registration.error;

import com.yolt.providers.common.exception.ProviderRequestFailedException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.registration.rest.error.DefaultRegistrationHttpErrorHandler;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.REGISTER;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultRegistrationHttpErrorHandlerTest {

    private DefaultRegistrationHttpErrorHandler errorHandler;

    @BeforeEach
    void initialize() {
        errorHandler = new DefaultRegistrationHttpErrorHandler();
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED", "FORBIDDEN", "BAD_REQUEST", "INTERNAL_SERVER_ERROR", "NOT_FOUND"})
    void shouldThrowProviderRequestFailedExceptionForRegisterPrometheusPath(String inputHttpStatus) {
        // given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.valueOf(inputHttpStatus));
        ExecutionInfo executionInfo = createExecutionInfo(REGISTER);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> errorHandler.handle(exception, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ProviderRequestFailedException.class);
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED", "FORBIDDEN", "BAD_REQUEST", "INTERNAL_SERVER_ERROR", "NOT_FOUND"})
    void shouldThrowProviderRequestFailedExceptionForUpdateRegistrationPrometheusPath(String inputHttpStatus) {
        // given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.valueOf(inputHttpStatus));
        ExecutionInfo executionInfo = createExecutionInfo("update_registration");

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> errorHandler.handle(exception, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ProviderRequestFailedException.class);
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED", "FORBIDDEN", "BAD_REQUEST", "INTERNAL_SERVER_ERROR", "NOT_FOUND"})
    void shouldThrowProviderRequestFailedExceptionForDeleteRegistrationPrometheusPath(String inputHttpStatus) {
        // given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.valueOf(inputHttpStatus));
        ExecutionInfo executionInfo = createExecutionInfo("delete_registration");

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> errorHandler.handle(exception, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ProviderRequestFailedException.class);
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED", "FORBIDDEN", "BAD_REQUEST", "INTERNAL_SERVER_ERROR", "NOT_FOUND"})
    void shouldHttpStatusCodeExceptionForUnknownPrometheusPath(String inputHttpStatus) {
        // given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.valueOf(inputHttpStatus));
        ExecutionInfo executionInfo = createExecutionInfo("wrong_path");

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> errorHandler.handle(exception, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(HttpStatusCodeException.class);
    }

    private ExecutionInfo createExecutionInfo(String prometheusPath) {
        return new ExecutionInfo("/example", HttpMethod.POST, new HttpHeaders(), prometheusPath);
    }
}
