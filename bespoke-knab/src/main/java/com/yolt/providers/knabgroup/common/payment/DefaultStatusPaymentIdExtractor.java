package com.yolt.providers.knabgroup.common.payment;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.StatusPaymentResponse;

public class DefaultStatusPaymentIdExtractor implements PaymentIdExtractor<StatusPaymentResponse, StatusPaymentPreExecutionResult> {

    @Override
    public String extractPaymentId(final StatusPaymentResponse statusPaymentResponse, final StatusPaymentPreExecutionResult preExecutionResult) {
        return preExecutionResult.getPaymentId();
    }
}
