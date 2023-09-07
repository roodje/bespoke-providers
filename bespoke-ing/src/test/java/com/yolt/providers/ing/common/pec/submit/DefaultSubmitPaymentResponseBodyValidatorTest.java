package com.yolt.providers.ing.common.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.ing.common.dto.PaymentStatusResponse;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DefaultSubmitPaymentResponseBodyValidatorTest {

    @InjectMocks
    private DefaultSubmitPaymentResponseBodyValidator sut;

    @Mock
    private JsonNode rawResponseBody;

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenPaymentStatusResponseIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> sut.validate(null, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction status response")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionIdIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> sut.validate(new PaymentStatusResponse(), rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction status")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenCorrectData() {
        // given
        var paymentStatusResponse = new PaymentStatusResponse("RCVD");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> sut.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

}