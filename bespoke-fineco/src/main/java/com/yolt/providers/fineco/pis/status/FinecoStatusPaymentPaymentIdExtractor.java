package com.yolt.providers.fineco.pis.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.fineco.dto.PaymentResponse;

public class FinecoStatusPaymentPaymentIdExtractor implements PaymentIdExtractor<PaymentResponse, FinecoStatusPaymentPreExecutionResult> {

    @Override
    public String extractPaymentId(PaymentResponse paymentStatusResponse, FinecoStatusPaymentPreExecutionResult preExecutionResult) {
        return preExecutionResult.getPaymentId();
    }
}
