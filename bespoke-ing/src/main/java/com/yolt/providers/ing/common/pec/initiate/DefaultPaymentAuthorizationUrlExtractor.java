package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;

public class DefaultPaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<InitiatePaymentResponse, DefaultInitiatePaymentPreExecutionResult> {

    @Override
    public String extractAuthorizationUrl(final InitiatePaymentResponse initiatePaymentResponse, final DefaultInitiatePaymentPreExecutionResult preExecution) {
        return initiatePaymentResponse.getLinks().getScaRedirect();
    }
}
