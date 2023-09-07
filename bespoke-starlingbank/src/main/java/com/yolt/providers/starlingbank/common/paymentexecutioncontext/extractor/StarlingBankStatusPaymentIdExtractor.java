package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.starlingbank.common.model.PaymentStatusResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;

public class StarlingBankStatusPaymentIdExtractor implements PaymentIdExtractor<PaymentStatusResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> {

    @Override
    public String extractPaymentId(PaymentStatusResponse paymentStatusResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult) throws PaymentExecutionTechnicalException {
        return preExecutionResult.getExternalPaymentId();
    }
}
