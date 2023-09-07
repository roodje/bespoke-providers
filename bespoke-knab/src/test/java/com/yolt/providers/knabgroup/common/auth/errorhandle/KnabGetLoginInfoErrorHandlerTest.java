package com.yolt.providers.knabgroup.common.auth.errorhandle;

import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.knabgroup.common.http.HttpErrorMessages.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class KnabGetLoginInfoErrorHandlerTest {

    private KnabGetLoginInfoHttpErrorHandler httpErrorHandler = new KnabGetLoginInfoHttpErrorHandler();

    @Test
    void shouldThrowGetLoginInfoUrlFailedExceptionWhenBadRequestStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(GetLoginInfoUrlFailedException.class)
                .isThrownBy(handleMethod)
                .withMessage(REQUEST_FORMED_INCORRECTLY_MESSAGE + HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldThrowGetLoginInfoUrlFailedExceptionWhenUnauthorizedStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(GetLoginInfoUrlFailedException.class)
                .isThrownBy(handleMethod)
                .withMessage(NOT_AUTHORIZED_MESSAGE + HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldThrowGetLoginInfoUrlFailedExceptionWhenForbiddenStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(GetLoginInfoUrlFailedException.class)
                .isThrownBy(handleMethod)
                .withMessage(ACCESS_FORBIDDEN_MESSAGE + HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldThrowGetLoginInfoUrlFailedExceptionWhenInternalServerErrorStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(GetLoginInfoUrlFailedException.class)
                .isThrownBy(handleMethod)
                .withMessage(ERROR_ON_THE_BANK_SIDE_MESSAGE + HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void shouldThrowGetLoginInfoUrlFailedExceptionWhenAnyOtherStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.FAILED_DEPENDENCY);

        //when
        ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(GetLoginInfoUrlFailedException.class)
                .isThrownBy(handleMethod)
                .withMessage(UNKNOWN_EXCEPTION_MESSAGE + HttpStatus.FAILED_DEPENDENCY.value());
    }
}