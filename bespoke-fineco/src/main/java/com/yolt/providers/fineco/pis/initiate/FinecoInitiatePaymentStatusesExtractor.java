package com.yolt.providers.fineco.pis.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.fineco.dto.PaymentResponse;
import com.yolt.providers.fineco.pis.FinecoPaymentStatusMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FinecoInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<PaymentResponse, FinecoInitiatePaymentPreExecutionResult> {

    private final FinecoPaymentStatusMapper statusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(PaymentResponse initiatePaymentResponse, FinecoInitiatePaymentPreExecutionResult initiatePecResult) {
        return statusMapper.mapPaymentStatus(initiatePaymentResponse.getTransactionStatus());
    }
}
