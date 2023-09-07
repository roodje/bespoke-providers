package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankInitiatePaymentExecutionContextPreExecutionResult;

public class StarlingBankInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<String, StarlingBankInitiatePaymentExecutionContextPreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(String responseBody, StarlingBankInitiatePaymentExecutionContextPreExecutionResult preExecutionResult) {
        return new PaymentStatuses(
                RawBankPaymentStatus.unknown("On this step there is no call to the bank, so by default INITIATION_SUCCESS is returned"),
                EnhancedPaymentStatus.INITIATION_SUCCESS);
    }
}
