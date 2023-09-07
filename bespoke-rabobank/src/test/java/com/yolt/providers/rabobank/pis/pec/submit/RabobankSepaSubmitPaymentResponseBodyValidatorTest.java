package com.yolt.providers.rabobank.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.rabobank.dto.external.StatusResponse;
import com.yolt.providers.rabobank.dto.external.TransactionStatus;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabobankSepaSubmitPaymentResponseBodyValidatorTest {

    private RabobankSepaSubmitPaymentResponseBodyValidator subject;

    @BeforeEach
    void setUp() {
        subject = new RabobankSepaSubmitPaymentResponseBodyValidator();
    }

    @Test
    void shouldValidateStatusResponse() throws ResponseBodyValidationException {
        //given
        JsonNode node = mock(JsonNode.class);
        StatusResponse statusResponse = mock(StatusResponse.class);
        when(statusResponse.getTransactionStatus()).thenReturn(TransactionStatus.ACCC);

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validate(statusResponse, node);

        //then
        assertThatCode(call).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenIncorrectResponseReceived() {
        //given
        JsonNode node = mock(JsonNode.class);
        StatusResponse statusResponse = mock(StatusResponse.class);
        when(statusResponse.getTransactionStatus()).thenReturn(null);

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validate(statusResponse, node);

        //then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(call)
                .withMessage("Missing transaction status")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(node));
    }
}
