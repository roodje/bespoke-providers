package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AbnAmroStatusesExtractor<PreExecutionResult> implements PaymentStatusesExtractor<TransactionStatusResponse, PreExecutionResult> {

    private final AbnAmroPaymentStatusMapper paymentStatusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(TransactionStatusResponse transactionStatusResponse, PreExecutionResult preExecutionResult) {
        return paymentStatusMapper.mapBankPaymentStatus(transactionStatusResponse.getStatus());
    }
}
