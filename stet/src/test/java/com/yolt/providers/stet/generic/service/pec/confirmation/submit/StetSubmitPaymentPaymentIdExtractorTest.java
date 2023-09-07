package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StetSubmitPaymentPaymentIdExtractorTest {

    private static final String PAYMENT_ID = "67117df1e2ca460c52084ca261aa85e8";

    private StetSubmitPaymentPaymentIdExtractor paymentIdExtractor;

    @BeforeEach
    void initialize() {
        paymentIdExtractor = new StetSubmitPaymentPaymentIdExtractor();
    }

    @Test
    void shouldExtractPaymentId() {
        // given
        StetPaymentStatusResponseDTO responseDTO = new StetPaymentStatusResponseDTO();
        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .paymentId(PAYMENT_ID)
                .build();

        // when
        String paymentId = paymentIdExtractor.extractPaymentId(responseDTO, preExecutionResult);

        // then
        assertThat(paymentId).isEqualTo(PAYMENT_ID);
    }
}
