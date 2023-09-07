package com.yolt.providers.stet.cicgroup.http.error;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.cicgroup.common.http.error.CicGroupHttpErrorHandler;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CicGroupHttpErrorHandlerTest {

    private HttpErrorHandler errorHandler = new CicGroupHttpErrorHandler();

    @ParameterizedTest
    @CsvSource({"Refresh token has expired", "Invalid refresh token"})
    void shouldThrowTokenInvalidExceptionForSpecificHttpStatusCodeExceptionWithBody(String body) {
        // given
        HttpStatus httpStatus = HttpStatus.valueOf(HttpStatus.BAD_REQUEST.value());
        ExecutionInfo executionInfo = createExecutionInfo();

        // when
        ThrowingCallable throwingCallable = () ->
                errorHandler.handle(new HttpClientErrorException(httpStatus, "fakeStatus", body.getBytes(), StandardCharsets.UTF_8), executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(TokenInvalidException.class);
    }

    private ExecutionInfo createExecutionInfo() {
        return new ExecutionInfo("/example", HttpMethod.POST, new HttpHeaders(), "prometheus_path");
    }
}
