package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;

public class RabobankSepaInitiatePaymentPaymentIdExtractor implements PaymentIdExtractor<InitiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult> {

    @Override
    public String extractPaymentId(InitiatedTransactionResponse initiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult preExecutionResult) {
        return initiatedTransactionResponse.getPaymentId().toString();
    }
}
