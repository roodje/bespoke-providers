package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;

public class DefaultInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<PaymentServiceProviderDraftPaymentResponse, DefaultInitiatePaymentPreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(PaymentServiceProviderDraftPaymentResponse response, DefaultInitiatePaymentPreExecutionResult preExecutionResult) {
        // We do not receive status during initiating payment. We have to assume that if everything works fine then payment initiation went fine
        return new PaymentStatuses(RawBankPaymentStatus.forStatus("INITIATED"), EnhancedPaymentStatus.INITIATION_SUCCESS);
    }
}
