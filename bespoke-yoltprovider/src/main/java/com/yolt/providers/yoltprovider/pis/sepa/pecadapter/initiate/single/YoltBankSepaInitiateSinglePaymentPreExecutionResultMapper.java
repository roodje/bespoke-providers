package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common.YoltBankSepaInitiatePaymentPreExecutionResult;

public class YoltBankSepaInitiateSinglePaymentPreExecutionResultMapper implements SepaInitiateSinglePaymentPreExecutionResultMapper<YoltBankSepaInitiatePaymentPreExecutionResult> {

    @Override
    public YoltBankSepaInitiatePaymentPreExecutionResult map(InitiatePaymentRequest initiatePaymentRequest) {
        return new YoltBankSepaInitiatePaymentPreExecutionResult(initiatePaymentRequest.getRequestDTO(),
                PaymentAuthenticationMeans.fromAuthenticationMeans(initiatePaymentRequest.getAuthenticationMeans()),
                initiatePaymentRequest.getSigner(),
                initiatePaymentRequest.getBaseClientRedirectUrl(),
                initiatePaymentRequest.getState());
    }
}
