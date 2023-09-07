package com.yolt.providers.stet.generic.service.fetchData.rest.error;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.fetchdata.rest.error.DefaultFetchDataHttpErrorHandler;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.PSU_IP_ADDRESS;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultFetchDataHttpErrorHandlerTest {

    private DefaultFetchDataHttpErrorHandler errorHandler;

    @BeforeEach
    void initialize() {
        errorHandler = new DefaultFetchDataHttpErrorHandler();
    }

    @Test
    void shouldThrowBackPressureRequestExceptionForTooManyRequestsOfHttpStatusCodeExceptionAndNotExistingPsuIpAddress() {
        // given
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpClientErrorException httpStatus = new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
        ExecutionInfo executionInfo = createExecutionInfo(httpHeaders);

        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(httpStatus, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(BackPressureRequestException.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionForTooManyRequestsOfHttpStatusCodeExceptionAndExistingPsuIpAddress() {
        // given
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(PSU_IP_ADDRESS, "127.0.0.1");

        HttpClientErrorException httpStatus = new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
        ExecutionInfo executionInfo = createExecutionInfo(httpHeaders);

        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(httpStatus, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(TokenInvalidException.class);
    }

    private ExecutionInfo createExecutionInfo(HttpHeaders headers) {
        return new ExecutionInfo("/example", HttpMethod.POST, headers, "prometheus_path");
    }
}
