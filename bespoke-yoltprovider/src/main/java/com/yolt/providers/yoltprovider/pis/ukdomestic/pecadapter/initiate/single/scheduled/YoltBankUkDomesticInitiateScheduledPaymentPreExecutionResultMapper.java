package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateScheduledPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;

public class YoltBankUkDomesticInitiateScheduledPaymentPreExecutionResultMapper implements UkInitiateScheduledPaymentPreExecutionResultMapper<YoltBankUkInitiateScheduledPaymentPreExecutionResult> {

    @Override
    public YoltBankUkInitiateScheduledPaymentPreExecutionResult map(InitiateUkDomesticScheduledPaymentRequest initiateUkDomesticPaymentRequest) {
        return new YoltBankUkInitiateScheduledPaymentPreExecutionResult(
                initiateUkDomesticPaymentRequest.getRequestDTO(),
                PaymentAuthenticationMeans.fromAuthenticationMeans(initiateUkDomesticPaymentRequest.getAuthenticationMeans()),
                initiateUkDomesticPaymentRequest.getSigner(),
                initiateUkDomesticPaymentRequest.getBaseClientRedirectUrl(),
                initiateUkDomesticPaymentRequest.getState(),
                null,
                null);
    }
}
