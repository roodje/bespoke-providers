package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;

public class StetStatusPaymentPaymentIdExtractor implements PaymentIdExtractor<StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> {

    @Override
    public String extractPaymentId(StetPaymentStatusResponseDTO responseDTO,
                                   StetConfirmationPreExecutionResult preExecutionResult) {
        return preExecutionResult.getPaymentId();
    }
}
