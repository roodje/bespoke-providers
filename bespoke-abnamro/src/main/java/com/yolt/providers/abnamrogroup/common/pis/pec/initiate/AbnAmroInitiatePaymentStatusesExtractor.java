package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.common.pis.InitiatePaymentResponseDTO;
import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AbnAmroInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<InitiatePaymentResponseDTO, AbnAmroInitiatePaymentPreExecutionResult> {

    private final AbnAmroPaymentStatusMapper paymentStatusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(InitiatePaymentResponseDTO initiatePaymentResponseDTO, AbnAmroInitiatePaymentPreExecutionResult abnAmroPaymentInitiationPreExecutionResult) {
        var status = TransactionStatusResponse.StatusEnum.fromValue(initiatePaymentResponseDTO.getStatus());
        return paymentStatusMapper.mapBankPaymentStatus(status);
    }
}
