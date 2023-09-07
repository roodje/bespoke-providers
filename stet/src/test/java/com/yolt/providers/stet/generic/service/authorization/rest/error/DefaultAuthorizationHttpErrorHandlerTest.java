package com.yolt.providers.stet.generic.service.authorization.rest.error;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultAuthorizationHttpErrorHandlerTest {

    private DefaultAuthorizationHttpErrorHandler errorHandler;

    @BeforeEach
    void initialize() {
        errorHandler = new DefaultAuthorizationHttpErrorHandler();
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED", "FORBIDDEN"})
    void shouldThrowTokenInvalidExceptionForSpecificHttpStatusCodeException(String inputHttpStatus) {
        // given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.valueOf(inputHttpStatus));
        ExecutionInfo executionInfo = createExecutionInfo();

        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(exception, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(TokenInvalidException.class);
    }

    @ParameterizedTest
    @CsvSource({"BAD_REQUEST", "INTERNAL_SERVER_ERROR", "NOT_FOUND"})
    void shouldThrowGetAccessTokenFailedExceptionForSpecificHttpStatusCodeException(String inputHttpStatus) {
        // given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.valueOf(inputHttpStatus));
        ExecutionInfo executionInfo = createExecutionInfo();

        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(exception, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(GetAccessTokenFailedException.class);
    }

    private ExecutionInfo createExecutionInfo() {
        return new ExecutionInfo("/example", HttpMethod.POST, new HttpHeaders(), "prometheus_path");
    }
}
