package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;

public class YoltBankUkDomesticInitiatePeriodicPaymentStatusesExtractor implements PaymentStatusesExtractor<InitiatePaymentConsentResponse, YoltBankUkInitiatePeriodicPaymentPreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(InitiatePaymentConsentResponse responseBody, YoltBankUkInitiatePeriodicPaymentPreExecutionResult preExecutionResult) {
        return new PaymentStatuses(
                RawBankPaymentStatus.unknown(),
                EnhancedPaymentStatus.INITIATION_SUCCESS
        );
    }
}
