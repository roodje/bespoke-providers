package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;

public class StarlingBankPaymentIdExtractor implements PaymentIdExtractor<PaymentSubmissionResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> {

    @Override
    public String extractPaymentId(PaymentSubmissionResponse paymentSubmissionResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult) throws PaymentExecutionTechnicalException {
        return paymentSubmissionResponse.getPaymentOrderUid().toString();
    }
}
