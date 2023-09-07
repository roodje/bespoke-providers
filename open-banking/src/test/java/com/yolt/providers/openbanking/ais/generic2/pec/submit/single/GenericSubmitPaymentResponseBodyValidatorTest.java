package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5Data;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GenericSubmitPaymentResponseBodyValidatorTest {

    @InjectMocks
    private GenericSubmitPaymentResponseBodyValidator subject;

    @Mock
    private JsonNode rawResponseBody;

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenDataIsMissingInResponse() {
        // given
        OBWriteDomesticResponse5 obWriteDomesticResponse5 = new OBWriteDomesticResponse5();

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticResponse5, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Data is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenDomesticPaymentIdIsMissingInData() {
        // given
        OBWriteDomesticResponse5 obWriteDomesticResponse5 = new OBWriteDomesticResponse5()
                .data(new OBWriteDomesticResponse5Data());

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticResponse5, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Domestic Payment ID is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenStatusIsMissingInData() {
        // given
        OBWriteDomesticResponse5 obWriteDomesticResponse5 = new OBWriteDomesticResponse5()
                .data(new OBWriteDomesticResponse5Data()
                        .domesticPaymentId("domesticPaymentId"));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticResponse5, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Status is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticResponse5 obWriteDomesticResponse5 = new OBWriteDomesticResponse5()
                .data(new OBWriteDomesticResponse5Data()
                        .domesticPaymentId("domesticPaymentId")
                        .status(OBWriteDomesticResponse5Data.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticResponse5, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }
}