package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentRequest;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class StetStatusPaymentStatusesExtractorTest {

    private StetStatusPaymentStatusesExtractor statusPaymentStatusesExtractor;

    @BeforeEach
    void initialize() {
        statusPaymentStatusesExtractor = new StetStatusPaymentStatusesExtractor();
    }

    @Test
    void shouldExtractAndMapProperlyPaymentStatuses() {
        StetPaymentStatus stetPaymentStatus = StetPaymentStatus.ACCP;
        StetPaymentStatusResponseDTO responseDTO = createStetPaymentStatusResponseDTO(stetPaymentStatus);
        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .build();

        // when
        PaymentStatuses paymentStatuses = statusPaymentStatusesExtractor.extractPaymentStatuses(responseDTO, preExecutionResult);

        // then
        assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);

        RawBankPaymentStatus expectedRawBankPaymentStatus = RawBankPaymentStatus.forStatus(String.valueOf(stetPaymentStatus), "");
        assertThat(paymentStatuses.getRawBankPaymentStatus()).isEqualToComparingFieldByField(expectedRawBankPaymentStatus);
    }

    @CsvSource({
            "PDNG,INITIATION_SUCCESS",
            "ACTC,INITIATION_SUCCESS",
            "RCVD,INITIATION_SUCCESS",
            "ACCP,ACCEPTED",
            "ACSP,ACCEPTED",
            "ACWC,ACCEPTED",
            "PART,ACCEPTED",
            "PATC,ACCEPTED",
            "ACWP,COMPLETED",
            "ACSC,COMPLETED",
            "CANC,NO_CONSENT_FROM_USER",
            "RJCT,REJECTED"})
    @ParameterizedTest
    void shouldMapToEnhancedPaymentStatus(String givenStetPaymentStatus, String expectedEnhancedPaymentStatus) {
        // given
        StetPaymentStatus stetPaymentStatus = StetPaymentStatus.valueOf(givenStetPaymentStatus);

        // when
        EnhancedPaymentStatus enhancedPaymentStatus = statusPaymentStatusesExtractor.mapToPaymentStatus(stetPaymentStatus);

        // then
        assertThat(enhancedPaymentStatus).isEqualTo(EnhancedPaymentStatus.valueOf(expectedEnhancedPaymentStatus));
    }

    private StetPaymentStatusResponseDTO createStetPaymentStatusResponseDTO(StetPaymentStatus stetPaymentStatus) {
        StetPaymentRequest stetPaymentRequest = new StetPaymentRequest();
        stetPaymentRequest.setPaymentInformationStatus(stetPaymentStatus);

        StetPaymentStatusResponseDTO responseDTO = new StetPaymentStatusResponseDTO();
        responseDTO.setPaymentRequest(stetPaymentRequest);
        return responseDTO;
    }
}
