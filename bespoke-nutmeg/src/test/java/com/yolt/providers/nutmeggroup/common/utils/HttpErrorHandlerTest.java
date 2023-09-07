package com.yolt.providers.nutmeggroup.common.utils;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.*;

public class HttpErrorHandlerTest {

    @Test
    public void testBadRequestErrorConversion() {
        assertThatThrownBy(() -> HttpErrorHandler.handleNon2xxResponseCode(BAD_REQUEST)).isInstanceOf(GetAccessTokenFailedException.class);
    }

    @Test
    public void testUnauthorizedErrorConversion() {
        assertThatThrownBy(() -> HttpErrorHandler.handleNon2xxResponseCode(UNAUTHORIZED)).isInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void testForbiddenErrorConversion() {
        assertThatThrownBy(() -> HttpErrorHandler.handleNon2xxResponseCode(FORBIDDEN)).isInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void testInternalServerErrorErrorConversion() {
        assertThatThrownBy(() -> HttpErrorHandler.handleNon2xxResponseCode(INTERNAL_SERVER_ERROR)).isInstanceOf(GetAccessTokenFailedException.class);
    }

    @Test
    public void testBadGatewayErrorConversion() {
        assertThatThrownBy(() -> HttpErrorHandler.handleNon2xxResponseCode(BAD_GATEWAY)).isInstanceOf(GetAccessTokenFailedException.class);
    }
}
