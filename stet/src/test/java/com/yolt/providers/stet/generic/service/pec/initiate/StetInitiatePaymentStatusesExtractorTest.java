package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StetInitiatePaymentStatusesExtractorTest {

    private StetInitiatePaymentStatusesExtractor initiatePaymentStatusesExtractor;

    @BeforeEach
    void initialize() {
        initiatePaymentStatusesExtractor = new StetInitiatePaymentStatusesExtractor();
    }

    @Test
    void shouldProvideProperlyExtractedPaymentStatuses() {
        // given
        StetPaymentInitiationResponseDTO responseDTO = new StetPaymentInitiationResponseDTO();
        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .build();

        // when
        PaymentStatuses paymentStatuses = initiatePaymentStatusesExtractor.extractPaymentStatuses(responseDTO, preExecutionResult);

        // then
        assertThat(paymentStatuses.getRawBankPaymentStatus()).isEqualToComparingFieldByField(RawBankPaymentStatus.unknown());
        assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
    }
}
