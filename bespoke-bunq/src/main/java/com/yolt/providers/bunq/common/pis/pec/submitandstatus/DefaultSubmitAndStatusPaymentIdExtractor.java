package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentStatusResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;

public class DefaultSubmitAndStatusPaymentIdExtractor implements PaymentIdExtractor<PaymentServiceProviderDraftPaymentStatusResponse, DefaultSubmitAndStatusPaymentPreExecutionResult> {

    @Override
    public String extractPaymentId(final PaymentServiceProviderDraftPaymentStatusResponse statusPaymentResponse, final DefaultSubmitAndStatusPaymentPreExecutionResult preExecutionResult) {
        return String.valueOf(preExecutionResult.getPaymentId());
    }
}
