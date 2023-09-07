package com.yolt.providers.ing.common.service;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.*;

public class HttpErrorHandlerTest {

    @ParameterizedTest
    @MethodSource("provideThrowableForHttpStatuses")
    public void shouldThrowProperExceptionForHandleNon2xxResponseCodeWithSpecificHttpStatus(HttpStatus httpStatus, Class<Throwable> throwableClass) {
        // when
        ThrowableAssert.ThrowingCallable handleNon2xxResponseCodeCallable = () -> HttpErrorHandler.handleNon2xxResponseCode(httpStatus);

        // then
        Assertions.assertThatThrownBy(handleNon2xxResponseCodeCallable)
                .isInstanceOf(throwableClass);
    }

    private static Stream<Arguments> provideThrowableForHttpStatuses() {
        return Stream.of(
            Arguments.of(BAD_REQUEST, GetAccessTokenFailedException.class),
            Arguments.of(UNAUTHORIZED, TokenInvalidException.class),
            Arguments.of(FORBIDDEN, TokenInvalidException.class),
            Arguments.of(INTERNAL_SERVER_ERROR, GetAccessTokenFailedException.class),
            Arguments.of(BAD_GATEWAY, GetAccessTokenFailedException.class)
        );
    }
}
