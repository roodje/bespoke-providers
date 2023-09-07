package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;

/**
 * In STET standard status is not returned during payment initiation.
 * Therefore, the status is hardcoded if the payment initiation was successful.
 */
public class StetInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(StetPaymentInitiationResponseDTO initiatePaymentResponse,
                                                  StetInitiatePreExecutionResult preExecutionResult) {
        return new PaymentStatuses(
                RawBankPaymentStatus.unknown(),
                EnhancedPaymentStatus.INITIATION_SUCCESS);
    }
}
