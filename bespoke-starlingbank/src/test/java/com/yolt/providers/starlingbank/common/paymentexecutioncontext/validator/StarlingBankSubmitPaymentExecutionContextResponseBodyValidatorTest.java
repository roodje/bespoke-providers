package com.yolt.providers.starlingbank.common.paymentexecutioncontext.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.starlingbank.common.model.ConsentInformation;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.validator.StarlingBankSubmitPaymentExecutionContextResponseBodyValidator;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class StarlingBankSubmitPaymentExecutionContextResponseBodyValidatorTest {
    private static final UUID PAYMENT_ORDER_UUID = UUID.randomUUID();

    @InjectMocks
    private StarlingBankSubmitPaymentExecutionContextResponseBodyValidator responseBodyValidator;

    @Mock
    private JsonNode rawBody;

    @Test
    void shouldNotThrowAnyExceptionWhenResponseBodyWithRequiredFieldsProvided() {
        // given
        PaymentSubmissionResponse paymentSubmissionResponse = new PaymentSubmissionResponse(
                PAYMENT_ORDER_UUID,
                new ConsentInformation()
        );

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(paymentSubmissionResponse, rawBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenResponseBodyNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(null, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Response body is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenPaymentOrderUIdIsMissing() {
        // given
        PaymentSubmissionResponse paymentSubmissionResponse = new PaymentSubmissionResponse(
                null,
                new ConsentInformation()
        );


        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(paymentSubmissionResponse, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing Payment Order UID")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }
}
