package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;

public class DefaultInitiatePaymentPaymentIdExtractor implements PaymentIdExtractor<InitiatePaymentResponse, DefaultInitiatePaymentPreExecutionResult> {

    @Override
    public String extractPaymentId(InitiatePaymentResponse initiatePaymentResponse, DefaultInitiatePaymentPreExecutionResult defaultInitiatePaymentPreExecutionResult) {
        return initiatePaymentResponse.getPaymentId();
    }
}
