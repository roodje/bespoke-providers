package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.yolt.providers.abnamrogroup.common.pis.pec.exception.AbnAmroConsentCancelledException;
import com.yolt.providers.common.exception.MissingDataException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class AbnAmroAuthorizationCodeExtractorTest {

    @InjectMocks
    private AbnAmroAuthorizationCodeExtractor subject;

    @Test
    void shouldReturnAuthorizationCodeForExtractAuthorizationCodeWhenCorrectData() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/callback?code=fakeCode";

        // when
        String result = subject.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThat(result).isEqualTo("fakeCode");
    }

    @Test
    void shouldThrowAbnAmroConsentCancelledExceptionForExtractAuthorizationCodeWhenErrorParameterInRedirectUrl() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/callback?error=Something went wrong";

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThatExceptionOfType(AbnAmroConsentCancelledException.class)
                .isThrownBy(callable)
                .withMessage("The user cancelled the consent process.");
    }

    @Test
    void shouldThrowMissingDataExceptionForExtractAuthorizationCodeWhenCodeParameterIsMissingInRedirectUrl() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/callback";

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThatExceptionOfType(MissingDataException.class)
                .isThrownBy(callable)
                .withMessage("Missing data for key code.");
    }
}