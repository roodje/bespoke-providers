package com.yolt.providers.openbanking.ais.aibgroup.common.http;


import com.yolt.providers.common.exception.TokenInvalidException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.Charset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class AibGroupRefreshTokenErrorHandlerTest {

    public AibGroupRefreshTokenErrorHandlerV2 handler = new AibGroupRefreshTokenErrorHandlerV2();

    @Test
    public void shouldThrowInvalidTokenExceptionOnHttp403() {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldThrowInvalidTokenExceptionOnHttp401() {
        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldThrowInvalidTokenExceptionOnHttp400WithExpiredConsentErrorCode() {
        // given
        String errorBodyAsString = "{" +
                "  \"path\": \"/security/v2/oauth/token\"," +
                "  \"status\": 400," +
                "  \"code\": \"400-1200-007\"," +
                "  \"uuid\": \"19a25073-143f-4929-9ec9-eefb8a42eb18\"," +
                "  \"timestamp\": 1607595399974" +
                "}";

        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                errorBodyAsString.getBytes(),
                Charset.defaultCharset()));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldThrowInvalidTokenExceptionOnHttp400WithInvalidRefreshTokenErrorCode() {
        // given
        String errorBodyAsString = "{ " +
                "   \"fault\": {" +
                "       \"faultstring\": \"Invalid Refresh Token\", " +
                "       \"detail\": { " +
                "           \"errorcode\": \"keymanagement.service.invalid_refresh_token\" " +
                "           } " +
                "   } " +
                "}";

        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                errorBodyAsString.getBytes(),
                Charset.defaultCharset()));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldThrowInvalidTokenExceptionOnHttp400WithStandardObInvalidGrantMessage() {
        // given
        String errorBodyAsString = "{" +
                "  \"status\": 400," +
                "  \"error\": \"invalid_grant\"," +
                "}";

        // when
        ThrowableAssert.ThrowingCallable errorHandler = () -> handler.handle(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                errorBodyAsString.getBytes(),
                Charset.defaultCharset()));

        // then
        assertThatThrownBy(errorHandler).isExactlyInstanceOf(TokenInvalidException.class);
    }


}