package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPaymentStatusesExtractor;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StetStatusPaymentStatusesExtractor extends StetConfirmationPaymentStatusesExtractor
        implements PaymentStatusesExtractor<StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(StetPaymentStatusResponseDTO responseDTO,
                                                  StetConfirmationPreExecutionResult preExecutionResult) {
        StetPaymentStatus paymentStatus = responseDTO.getPaymentRequest().getPaymentInformationStatus();
        return mapToPaymentStatuses(paymentStatus);
    }
}
