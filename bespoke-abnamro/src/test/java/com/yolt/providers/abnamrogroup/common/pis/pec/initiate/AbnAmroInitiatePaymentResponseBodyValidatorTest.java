package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamrogroup.common.pis.InitiatePaymentResponseDTO;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AbnAmroInitiatePaymentResponseBodyValidatorTest {

    @InjectMocks
    private AbnAmroInitiatePaymentResponseBodyValidator subject;

    @Mock
    private JsonNode rawResponseBody;

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenInitiatePaymentResponseDTOIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(null, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing response initiation object")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionIdIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(new InitiatePaymentResponseDTO(), rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction ID")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenCorrectData() {
        // given
        InitiatePaymentResponseDTO initiatePaymentResponseDTO = new InitiatePaymentResponseDTO("", "transactionId", "", "", "");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(initiatePaymentResponseDTO, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }
}