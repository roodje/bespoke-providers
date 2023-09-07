package com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.http.VanquisGroupErrorHandlerV2;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.Charset;

import static com.yolt.providers.openbanking.ais.vanquisgroup.common.http.VanquisGroupErrorHandlerV2.VANQUIS_GROUP_ERROR_HANDLER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class VanquisGroupErrorHandlerV2Test {

    private VanquisGroupErrorHandlerV2 handler = VANQUIS_GROUP_ERROR_HANDLER;

    @Test
    public void shouldThrowInvalidTokenExceptionOnStatus400WithInvalidGrantErrorDescription() {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "BAD REQUEST",
                "{\"error\":\"invalid_grant\",\"error_description\":\"invalid grant error\"}".getBytes(),
                Charset.defaultCharset()));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldThrowInvalidTokenExceptionOnStatus400WithInvalidGrant() {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "BAD REQUEST",
                "{\"error\":\"invalid_grant\",\"error_description\":\"unknown, invalid, or expired refresh token\"}".getBytes(),
                Charset.defaultCharset()));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldThrowInvalidTokenExceptionOnStatus401() {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldThrowInvalidTokenExceptionOnStatus403() {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }
}
