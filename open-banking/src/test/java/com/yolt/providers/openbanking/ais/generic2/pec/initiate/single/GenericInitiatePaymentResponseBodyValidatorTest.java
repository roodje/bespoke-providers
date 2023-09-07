package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5Data;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GenericInitiatePaymentResponseBodyValidatorTest {

    @InjectMocks
    private GenericInitiatePaymentResponseBodyValidator subject;

    @Mock
    private JsonNode rawBodyResponse;

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenDataIsMissingInResponse() {
        // given
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5();

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticConsentResponse5, rawBodyResponse);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Data is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBodyResponse));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenConsentIsMissingInData() {
        // given
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data());

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticConsentResponse5, rawBodyResponse);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Consent ID is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBodyResponse));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenStatusIsMissingInData() {
        // given
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data()
                        .consentId("consentId"));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticConsentResponse5, rawBodyResponse);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Status is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBodyResponse));
    }

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data()
                        .consentId("consentId")
                        .status(OBWriteDomesticConsentResponse5Data.StatusEnum.AUTHORISED));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(obWriteDomesticConsentResponse5, rawBodyResponse);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }
}