package com.yolt.providers.rabobank.pis.pec.submit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RabobankSepaSubmitPaymentPaymentIdExtractorTest {

    private RabobankSepaSubmitPaymentPaymentIdExtractor subject;

    @BeforeEach
    void setUp() {
        subject = new RabobankSepaSubmitPaymentPaymentIdExtractor();
    }

    @Test
    void shouldReturnPaymentId() {
        //given
        RabobankSepaSubmitPaymentPreExecutionResult preExecutionResult = new RabobankSepaSubmitPaymentPreExecutionResult(
                "12345paymentId6666",
                null,
                null,
                null,
                null);

        //when
        String returnedPaymentId = subject.extractPaymentId(null, preExecutionResult);

        //then
        assertThat(returnedPaymentId).isEqualTo("12345paymentId6666");
    }
}
