package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StarlingBankSubmitPaymentStatusesExtractor implements PaymentStatusesExtractor<PaymentSubmissionResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(PaymentSubmissionResponse responseBody, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult) {

        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus("ACCEPTED", "" ),
                EnhancedPaymentStatus.ACCEPTED);
    }
}
