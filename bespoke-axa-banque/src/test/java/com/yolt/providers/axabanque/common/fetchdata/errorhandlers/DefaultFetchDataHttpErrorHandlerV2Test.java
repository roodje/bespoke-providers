package com.yolt.providers.axabanque.common.fetchdata.errorhandlers;

import com.yolt.providers.axabanque.common.fetchdata.errorhandler.DefaultFetchDataHttpErrorHandlerV2;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;


class DefaultFetchDataHttpErrorHandlerV2Test {

    private static final String REQUEST_FORMED_INCORRECTLY_MESSAGE = "Request formed incorrectly: HTTP ";
    private static final String NOT_AUTHORIZED_MESSAGE = "We are not authorized to call endpoint: HTTP ";
    private static final String ACCESS_FORBIDDEN_MESSAGE = "Access to call is forbidden: HTTP ";
    private static final String ERROR_ON_THE_BANK_SIDE_MESSAGE = "Something went wrong on bank side: HTTP ";
    private static final String UNKNOWN_EXCEPTION_MESSAGE = "Unknown exception: HTTP ";
    private static final String TOO_MANY_REQUESTS = "Too many requests: Http status ";

    DefaultFetchDataHttpErrorHandlerV2 httpErrorHandler = new DefaultFetchDataHttpErrorHandlerV2(new DefaultHttpErrorHandlerV2());

    @Test
    void shouldProperlyHandleWithTokenInvalidExceptionAndProviderHttpStatusException() {

        assertThrows(ProviderHttpStatusException.class, () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.BAD_REQUEST), null));
        assertThrows(TokenInvalidException.class, () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.UNAUTHORIZED), null));
        assertThrows(TokenInvalidException.class, () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.FORBIDDEN), null));
        assertThrows(ProviderHttpStatusException.class, () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR), null));
        assertThrows(ProviderHttpStatusException.class, () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT), null));
        assertThrows(BackPressureRequestException.class, () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS), null));
        assertThrows(ProviderHttpStatusException.class, () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS), "123"));
    }

    @Test
    void shouldThrowProviderHttpStatusExceptionWhenBadRequestStatusIsReturned() {

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.BAD_REQUEST), null);

        //then
        assertThatExceptionOfType(ProviderHttpStatusException.class)
                .isThrownBy(handleMethod)
                .withMessage(REQUEST_FORMED_INCORRECTLY_MESSAGE + HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenUnauthorizedStatusIsReturned() {

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.UNAUTHORIZED), null);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(handleMethod)
                .withMessage(NOT_AUTHORIZED_MESSAGE + HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenForbiddenStatusIsReturned() {
        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.FORBIDDEN), null);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(handleMethod)
                .withMessage(ACCESS_FORBIDDEN_MESSAGE + HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldThrowProviderHttpStatusExceptionWhenInternalServerErrorStatusIsReturned() {

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR), null);

        //then
        assertThatExceptionOfType(ProviderHttpStatusException.class)
                .isThrownBy(handleMethod)
                .withMessage(ERROR_ON_THE_BANK_SIDE_MESSAGE + HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void shouldThrowBackPressureRequestExceptionWhenTooManyRequest() {
        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS), null);

        //then
        assertThatExceptionOfType(BackPressureRequestException.class)
                .isThrownBy(handleMethod)
                .withMessage(TOO_MANY_REQUESTS + HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void shouldThrowFetchExceptionWhenTooManyRequestAndMadeWithPsuIpAdderess() {

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS), "123");

        //then
        assertThatExceptionOfType(ProviderHttpStatusException.class)
                .isThrownBy(handleMethod)
                .withMessage(TOO_MANY_REQUESTS + HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void shouldThrowProviderHttpStatusExceptionWhenAnyOtherStatusIsReturned() {
        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> httpErrorHandler.handle(new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT), null);

        //then
        assertThatExceptionOfType(ProviderHttpStatusException.class)
                .isThrownBy(handleMethod)
                .withMessage(UNKNOWN_EXCEPTION_MESSAGE + HttpStatus.I_AM_A_TEAPOT.value());
    }
}
