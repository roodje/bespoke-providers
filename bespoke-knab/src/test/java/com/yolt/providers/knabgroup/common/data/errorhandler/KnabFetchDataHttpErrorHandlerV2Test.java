package com.yolt.providers.knabgroup.common.data.errorhandler;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.knabgroup.common.exception.KnabGroupFetchDataException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.knabgroup.common.http.HttpErrorMessages.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KnabFetchDataHttpErrorHandlerV2Test {

    private KnabFetchDataHttpErrorHandlerV2 httpErrorHandler = new KnabFetchDataHttpErrorHandlerV2();


    @Test
    void shouldThrowKnabGroupFetchDataExceptionWhenBadRequestStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(KnabGroupFetchDataException.class)
                .isThrownBy(handleMethod)
                .withMessage(REQUEST_FORMED_INCORRECTLY_MESSAGE + HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenUnauthorizedStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(handleMethod)
                .withMessage(NOT_AUTHORIZED_MESSAGE + HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenForbiddenStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(handleMethod)
                .withMessage(ACCESS_FORBIDDEN_MESSAGE + HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldThrowKnabGroupFetchDataExceptionWhenInternalServerErrorStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(KnabGroupFetchDataException.class)
                .isThrownBy(handleMethod)
                .withMessage(ERROR_ON_THE_BANK_SIDE_MESSAGE + HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void shouldThrowKnabGroupFetchDataExceptionWhenAnyOtherStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.FAILED_DEPENDENCY);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(KnabGroupFetchDataException.class)
                .isThrownBy(handleMethod)
                .withMessage(UNKNOWN_EXCEPTION_MESSAGE + HttpStatus.FAILED_DEPENDENCY.value());
    }
}