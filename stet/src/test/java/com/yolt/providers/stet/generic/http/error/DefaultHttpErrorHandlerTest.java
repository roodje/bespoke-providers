package com.yolt.providers.stet.generic.http.error;

import com.yolt.providers.common.exception.ProviderHttpStatusException;
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

class DefaultHttpErrorHandlerTest {

    private DefaultHttpErrorHandler errorHandler;

    @BeforeEach
    void initialize() {
        errorHandler = new DefaultHttpErrorHandler();
    }

    @ParameterizedTest
    @CsvSource({"BAD_REQUEST", "INTERNAL_SERVER_ERROR", "NOT_FOUND"})
    void shouldThrowProviderHttpStatusExceptionForSpecificHttpStatusCodeException(String inputHttpStatus) {
        // given
        HttpStatus httpStatus = HttpStatus.valueOf(inputHttpStatus);
        ExecutionInfo executionInfo = createExecutionInfo();

        // when
        ThrowingCallable throwingCallable = () ->
                errorHandler.handle(new HttpClientErrorException(httpStatus), executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ProviderHttpStatusException.class);
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED", "FORBIDDEN"})
    void shouldThrowTokenInvalidExceptionForSpecificHttpStatusCodeException(String inputHttpStatus) {
        // given
        HttpStatus httpStatus = HttpStatus.valueOf(inputHttpStatus);
        ExecutionInfo executionInfo = createExecutionInfo();

        // when
        ThrowingCallable throwingCallable = () ->
                errorHandler.handle(new HttpClientErrorException(httpStatus), executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(TokenInvalidException.class);
    }

    private ExecutionInfo createExecutionInfo() {
        return new ExecutionInfo("/example", HttpMethod.POST, new HttpHeaders(), "prometheus_path");
    }
}
