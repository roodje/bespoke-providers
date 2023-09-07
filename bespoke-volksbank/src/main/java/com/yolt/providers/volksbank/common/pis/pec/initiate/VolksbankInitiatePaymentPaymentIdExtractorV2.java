package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;

public class VolksbankInitiatePaymentPaymentIdExtractorV2 implements PaymentIdExtractor<InitiatePaymentResponse, VolksbankSepaInitiatePreExecutionResult> {

    @Override
    public String extractPaymentId(InitiatePaymentResponse initiatePaymentResponse, VolksbankSepaInitiatePreExecutionResult volksbankSepaInitiatePreExecutionResult) {
        return initiatePaymentResponse.getPaymentId();
    }
}
