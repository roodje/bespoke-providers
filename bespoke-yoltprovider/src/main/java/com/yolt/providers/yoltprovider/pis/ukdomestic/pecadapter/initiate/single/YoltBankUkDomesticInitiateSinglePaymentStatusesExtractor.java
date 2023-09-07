package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;

public class YoltBankUkDomesticInitiateSinglePaymentStatusesExtractor implements PaymentStatusesExtractor<InitiatePaymentConsentResponse, YoltBankUkInitiateSinglePaymentPreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(InitiatePaymentConsentResponse responseBody, YoltBankUkInitiateSinglePaymentPreExecutionResult preExecutionResult) {
        return new PaymentStatuses(
                RawBankPaymentStatus.unknown(),
                EnhancedPaymentStatus.INITIATION_SUCCESS
        );
    }
}
