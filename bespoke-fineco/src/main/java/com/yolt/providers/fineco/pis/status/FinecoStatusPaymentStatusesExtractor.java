package com.yolt.providers.fineco.pis.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.fineco.dto.PaymentResponse;
import com.yolt.providers.fineco.pis.FinecoPaymentStatusMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FinecoStatusPaymentStatusesExtractor implements PaymentStatusesExtractor<PaymentResponse, FinecoStatusPaymentPreExecutionResult> {

    private final FinecoPaymentStatusMapper statusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(PaymentResponse paymentStatusResponse, FinecoStatusPaymentPreExecutionResult defaultSubmitPaymentPreExecutionResult) {
        return statusMapper.mapPaymentStatus(paymentStatusResponse.getTransactionStatus());
    }
}
