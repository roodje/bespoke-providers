package com.yolt.providers.openbanking.ais.barclaysgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.Charset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class BarclaysGroupErrorHandlerV2Test {

    public BarclaysGroupErrorHandlerV2 handler = new BarclaysGroupErrorHandlerV2();

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
    public void shouldThrowInvalidTokenExceptionOnHttp400WithInvalidConsentMessage() {
        // given
        String errorBodyAsString = "{" +
                "  \"Code\": \"400 Bad Request\"," +
                "  \"Id\": \"<uuid>\"," +
                "  \"Message\": \"Consent validation failed. \"," +
                "  \"Errors\": [" +
                "    {" +
                "      \"ErrorCode\": \"UK.OBIE.Resource.InvalidConsentStatus\"," +
                "      \"Message\": \"The requested Consent ID doesn't exist or do not have valid status. \"" +
                "    }" +
                "  ]" +
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