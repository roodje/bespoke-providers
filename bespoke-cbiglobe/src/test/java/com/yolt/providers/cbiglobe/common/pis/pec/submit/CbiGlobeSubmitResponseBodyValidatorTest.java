package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.cbiglobe.common.model.TransactionStatus;
import com.yolt.providers.cbiglobe.pis.dto.GetPaymentStatusRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CbiGlobeSubmitResponseBodyValidatorTest {

    @InjectMocks
    private CbiGlobeSubmitResponseBodyValidator subject;

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionStatusIsMissingInResponse() {
        // given
        var paymentStatusResponse = new GetPaymentStatusRequestResponseType();
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction status")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenTransactionStatusIsProvidedInResponse() {
        // given
        var paymentStatusResponse = new GetPaymentStatusRequestResponseType();
        paymentStatusResponse.setTransactionStatus(TransactionStatus.RCVD.toString());
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }
}