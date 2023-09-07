package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;

public class AbnAmroPaymentIdExtractor<PreExecutionResult> implements PaymentIdExtractor<TransactionStatusResponse, PreExecutionResult> {

    @Override
    public String extractPaymentId(TransactionStatusResponse transactionStatusResponse, PreExecutionResult preExecutionResult) {
        return transactionStatusResponse.getTransactionId();
    }
}
