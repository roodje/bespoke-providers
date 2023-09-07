package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;

public class RabobankSepaInitiatePaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<InitiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult> {

    @Override
    public String extractAuthorizationUrl(InitiatedTransactionResponse initiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult preExecutionResult) {
        return initiatedTransactionResponse.getLinks().getScaRedirect().getHref();
    }
}
