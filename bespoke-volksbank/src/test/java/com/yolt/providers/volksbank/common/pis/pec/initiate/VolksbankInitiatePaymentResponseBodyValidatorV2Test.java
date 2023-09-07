package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VolksbankInitiatePaymentResponseBodyValidatorV2Test {

    @InjectMocks
    private VolksbankInitiatePaymentResponseBodyValidatorV2 subject;

    @Test
    void shouldThrowResponseBodyValidationExceptionWithProperMessageForValidateWhenPaymentIdIsMissingInResponse() {
        // given
        var initiatePaymentResponse = new InitiatePaymentResponse();
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(initiatePaymentResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Payment ID is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenPaymentIdInResponse() {
        // given
        var initiatePaymentResponse = new InitiatePaymentResponse();
        initiatePaymentResponse.setPaymentId("fakePaymentId");
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(initiatePaymentResponse, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }
}