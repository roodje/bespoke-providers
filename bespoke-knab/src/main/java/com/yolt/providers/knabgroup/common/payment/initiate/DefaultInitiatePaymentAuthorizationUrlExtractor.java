package com.yolt.providers.knabgroup.common.payment.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentResponse;

public class DefaultInitiatePaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<InitiatePaymentResponse, InitiatePaymentPreExecutionResult> {

    @Override
    public String extractAuthorizationUrl(final InitiatePaymentResponse response, final InitiatePaymentPreExecutionResult preExecutionResult) {
        return response.getRedirectUrl();
    }
}
