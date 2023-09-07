package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;

public class YoltBankUkDomesticInitiateScheduledPaymentStatusesExtractor implements PaymentStatusesExtractor<InitiatePaymentConsentResponse, YoltBankUkInitiateScheduledPaymentPreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(InitiatePaymentConsentResponse responseBody, YoltBankUkInitiateScheduledPaymentPreExecutionResult preExecutionResult) {
        return new PaymentStatuses(
                RawBankPaymentStatus.unknown(),
                EnhancedPaymentStatus.INITIATION_SUCCESS
        );
    }
}
