package com.yolt.providers.rabobank.pis.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.rabobank.dto.external.StatusResponse;

public class RabobankSepaSubmitPaymentPaymentIdExtractor implements PaymentIdExtractor<StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult> {

    @Override
    public String extractPaymentId(StatusResponse statusResponse, RabobankSepaSubmitPaymentPreExecutionResult preExecutionResult) {
        return preExecutionResult.getPaymentId();
    }
}
