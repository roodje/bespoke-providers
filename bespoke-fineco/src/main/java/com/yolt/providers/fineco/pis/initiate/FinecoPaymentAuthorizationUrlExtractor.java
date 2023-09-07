package com.yolt.providers.fineco.pis.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.fineco.dto.PaymentResponse;

public class FinecoPaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<PaymentResponse, FinecoInitiatePaymentPreExecutionResult> {

    @Override
    public String extractAuthorizationUrl(PaymentResponse initiatePaymentResponse, FinecoInitiatePaymentPreExecutionResult preExecution) {
        return initiatePaymentResponse.getLinks().getScaRedirect();
    }
}
