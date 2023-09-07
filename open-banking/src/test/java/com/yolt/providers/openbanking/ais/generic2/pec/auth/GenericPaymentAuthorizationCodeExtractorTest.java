package com.yolt.providers.openbanking.ais.generic2.pec.auth;

import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.PaymentCancelledException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class GenericPaymentAuthorizationCodeExtractorTest {

    @InjectMocks
    private GenericPaymentAuthorizationCodeExtractor subject;

    @Test
    void shouldReturnAuthorizationCodeCodeWhenCorrectDataAreProvided() throws ConfirmationFailedException {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/callback?code=123";

        // when
        String result = subject.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThat(result).isEqualTo("123");
    }

    @Test
    void shouldThrowPaymentCancelledExceptionWhenAccessDeniedErrorInQueryParams() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/callback?error=access_denied";

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThatExceptionOfType(PaymentCancelledException.class)
                .isThrownBy(callable)
                .withMessage("Got error in redirect URL: access_denied");
    }

    @Test
    void shouldThrowPaymentCancelledExceptionWhenAccessDeniedErrorInFragment() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/callback#error=access_denied";

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThatExceptionOfType(PaymentCancelledException.class)
                .isThrownBy(callable)
                .withMessage("Got error in redirect URL: access_denied");
    }

    @Test
    void shouldThrowConfirmationFailedExceptionWhenErrorOtherThanAccessDeniedInQueryParams() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/callback?error=other";

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThatExceptionOfType(ConfirmationFailedException.class)
                .isThrownBy(callable)
                .withMessage("Got error in redirect URL: other");
    }

    @Test
    void shouldThrowConfirmationFailedExceptionWhenErrorOtherThanAccessDeniedInFragment() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/callback#error=other";

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThatExceptionOfType(ConfirmationFailedException.class)
                .isThrownBy(callable)
                .withMessage("Got error in redirect URL: other");
    }
}