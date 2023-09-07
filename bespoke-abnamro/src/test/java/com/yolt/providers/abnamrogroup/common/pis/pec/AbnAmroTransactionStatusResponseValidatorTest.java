package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class AbnAmroTransactionStatusResponseValidatorTest {

    @InjectMocks
    private AbnAmroTransactionStatusResponseValidator subject;

    @Mock
    private JsonNode rawResponseBody;

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionStatusResponseIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(null, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction status response");
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionIdIsMissing() {
        // given
        TransactionStatusResponse transactionStatusResponse = new TransactionStatusResponse();
        transactionStatusResponse.setStatus(TransactionStatusResponse.StatusEnum.STORED);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(transactionStatusResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction ID");
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenStatusIsNull() {
        // given
        TransactionStatusResponse transactionStatusResponse = new TransactionStatusResponse();
        transactionStatusResponse.setTransactionId("trxId");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(transactionStatusResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction status");
    }
}