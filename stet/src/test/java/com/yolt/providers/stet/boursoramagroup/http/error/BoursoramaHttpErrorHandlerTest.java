package com.yolt.providers.stet.boursoramagroup.http.error;

import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.boursoramagroup.common.http.error.BoursoramaHttpErrorHandler;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoursoramaHttpErrorHandlerTest {

    private HttpErrorHandler errorHandler;

    @BeforeEach
    void initialize() {
        errorHandler = new BoursoramaHttpErrorHandler();
    }

    @ParameterizedTest
    @CsvSource({"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
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
    @CsvSource({"UNAUTHORIZED", "FORBIDDEN", "NOT_FOUND"})
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

    @Test
    void shouldThrowTokenInvalidExceptionForBadRequestWithRequestTokenExpiredMessageInBody() {
        // given
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ExecutionInfo executionInfo = createExecutionInfo();
        String body = """
                        { "timestamp": "Fri, 17 Sep 2021 06:44:10 +0200", "status": 400, "error": "Bad Request", "message": "Refresh Token expired", "path": "\\/services\\/api\\/v1.7\\/_public_\\/authentication\\/oauth\\/refreshtoken" }"
                """;
        HttpClientErrorException httpClientErrorException = new HttpClientErrorException(httpStatus, "", body.getBytes(), Charset.defaultCharset());

        // when
        ThrowingCallable throwingCallable = () ->
                errorHandler.handle(httpClientErrorException, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(TokenInvalidException.class);
    }

    private ExecutionInfo createExecutionInfo() {
        return new ExecutionInfo("/example", HttpMethod.POST, new HttpHeaders(), "prometheus_path");
    }
}
