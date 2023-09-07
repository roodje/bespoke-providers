package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.volksbank.dto.v1_1.PaymentStatus;

public class VolksbankSubmitPaymentPaymentIdExtractorV2 implements PaymentIdExtractor<PaymentStatus, VolksbankSepaSubmitPreExecutionResult> {

    @Override
    public String extractPaymentId(PaymentStatus paymentStatusResponse, VolksbankSepaSubmitPreExecutionResult preExecutionResult) {
        return preExecutionResult.getPaymentId();
    }
}
