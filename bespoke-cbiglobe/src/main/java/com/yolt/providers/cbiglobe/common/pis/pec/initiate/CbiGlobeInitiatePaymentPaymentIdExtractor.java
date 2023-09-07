package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;

public class CbiGlobeInitiatePaymentPaymentIdExtractor implements PaymentIdExtractor<PaymentInitiationRequestResponseType, CbiGlobeSepaInitiatePreExecutionResult> {

    @Override
    public String extractPaymentId(PaymentInitiationRequestResponseType initiatePaymentResponse, CbiGlobeSepaInitiatePreExecutionResult cbiGlobeSepaInitiatePreExecutionResult) {
        return initiatePaymentResponse.getPaymentId();
    }
}
