package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5Data;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GenericSubmitScheduledPaymentResponseBodyValidatorTest {

    @InjectMocks
    private GenericSubmitScheduledPaymentResponseBodyValidator subject;

    @Mock
    private JsonNode rawResponseBody;

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenDataIsMissingInResponse() {
        // given
        OBWriteDomesticScheduledResponse5 obWriteDomesticScheduledResponse5 = new OBWriteDomesticScheduledResponse5();

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticScheduledResponse5, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Data is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenDomesticPaymentIdIsMissingInData() {
        // given
        OBWriteDomesticScheduledResponse5 obWriteDomesticScheduledResponse5 = new OBWriteDomesticScheduledResponse5()
                .data(new OBWriteDomesticScheduledResponse5Data());

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticScheduledResponse5, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Domestic Payment ID is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenStatusIsMissingInData() {
        // given
        OBWriteDomesticScheduledResponse5 obWriteDomesticScheduledResponse5 = new OBWriteDomesticScheduledResponse5()
                .data(new OBWriteDomesticScheduledResponse5Data()
                        .domesticScheduledPaymentId("domesticPaymentId"));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticScheduledResponse5, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Status is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticScheduledResponse5 obWriteDomesticScheduledResponse5 = new OBWriteDomesticScheduledResponse5()
                .data(new OBWriteDomesticScheduledResponse5Data()
                        .domesticScheduledPaymentId("domesticPaymentId")
                        .status(OBWriteDomesticScheduledResponse5Data.StatusEnum.INITIATIONCOMPLETED));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticScheduledResponse5, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }
}