package com.yolt.providers.ing.common.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.ing.common.dto.PaymentStatusResponse;

public class DefaultSubmitPaymentPaymentIdExtractor implements PaymentIdExtractor<PaymentStatusResponse, DefaultSubmitPaymentPreExecutionResult> {

    @Override
    public String extractPaymentId(final PaymentStatusResponse paymentStatusResponse, final DefaultSubmitPaymentPreExecutionResult preExecutionResult) {
        return preExecutionResult.getPaymentId();
    }
}
