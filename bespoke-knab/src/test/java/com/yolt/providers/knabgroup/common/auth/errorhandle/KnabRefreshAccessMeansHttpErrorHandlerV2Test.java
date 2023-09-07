package com.yolt.providers.knabgroup.common.auth.errorhandle;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.knabgroup.common.http.HttpErrorMessages.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class KnabRefreshAccessMeansHttpErrorHandlerV2Test {

    private KnabRefreshAccessMeansHttpErrorHandlerV2 httpErrorHandler = new KnabRefreshAccessMeansHttpErrorHandlerV2();

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhenBadRequestStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        given(exception.getStatusCode()).willReturn(HttpStatus.BAD_REQUEST);
        given(exception.getResponseBodyAsString()).willReturn("Error message");

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(handleMethod)
                .withMessage(REQUEST_FORMED_INCORRECTLY_MESSAGE + HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenUnauthorizedStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        given(exception.getStatusCode()).willReturn(HttpStatus.UNAUTHORIZED);
        given(exception.getResponseBodyAsString()).willReturn("Error message");

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(handleMethod)
                .withMessage(NOT_AUTHORIZED_MESSAGE + HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenBadRequestStatusIsReturnedWithInvalidGrantMessage() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        given(exception.getStatusCode()).willReturn(HttpStatus.BAD_REQUEST);
        given(exception.getResponseBodyAsString()).willReturn("invalid_grant");

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(handleMethod)
                .withMessage("Invalid grant: HTTP 400");
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenForbiddenStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        given(exception.getStatusCode()).willReturn(HttpStatus.FORBIDDEN);
        given(exception.getResponseBodyAsString()).willReturn("Error message");

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(handleMethod)
                .withMessage(ACCESS_FORBIDDEN_MESSAGE + HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhenInternalServerErrorStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        given(exception.getStatusCode()).willReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        given(exception.getResponseBodyAsString()).willReturn("Error message");

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(handleMethod)
                .withMessage(ERROR_ON_THE_BANK_SIDE_MESSAGE + HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhenAnyOtherStatusIsReturned() {
        //given
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        given(exception.getStatusCode()).willReturn(HttpStatus.FAILED_DEPENDENCY);
        given(exception.getResponseBodyAsString()).willReturn("Error message");

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(exception);

        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(handleMethod)
                .withMessage(UNKNOWN_EXCEPTION_MESSAGE + HttpStatus.FAILED_DEPENDENCY.value());
    }
}