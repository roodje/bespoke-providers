package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiatePeriodicPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPeriodicPaymentRequest;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;

public class YoltBankUkDomesticInitiatePeriodicPaymentPreExecutionResultMapper implements UkInitiatePeriodicPaymentPreExecutionResultMapper<YoltBankUkInitiatePeriodicPaymentPreExecutionResult> {

    @Override
    public YoltBankUkInitiatePeriodicPaymentPreExecutionResult map(InitiateUkDomesticPeriodicPaymentRequest initiateUkDomesticPaymentRequest) {
        return new YoltBankUkInitiatePeriodicPaymentPreExecutionResult(
                initiateUkDomesticPaymentRequest.getRequestDTO(),
                PaymentAuthenticationMeans.fromAuthenticationMeans(initiateUkDomesticPaymentRequest.getAuthenticationMeans()),
                initiateUkDomesticPaymentRequest.getSigner(),
                initiateUkDomesticPaymentRequest.getBaseClientRedirectUrl(),
                initiateUkDomesticPaymentRequest.getState(),
                null,
                null);
    }
}
