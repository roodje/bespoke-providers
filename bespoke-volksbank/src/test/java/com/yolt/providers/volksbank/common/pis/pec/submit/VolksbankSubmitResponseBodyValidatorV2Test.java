package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.volksbank.dto.v1_1.PaymentStatus;
import com.yolt.providers.volksbank.dto.v1_1.TransactionStatus;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VolksbankSubmitResponseBodyValidatorV2Test {

    @InjectMocks
    private VolksbankSubmitResponseBodyValidatorV2 subject;

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionStatusIsMissingInResponse() {
        // given
        var paymentStatus = new PaymentStatus();
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatus, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction status")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenTransactionStatusIsProvidedInResponse() {
        // given
        var paymentStatus = new PaymentStatus();
        paymentStatus.setTransactionStatus(TransactionStatus.RCVD);
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatus, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }
}