package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateSinglePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.YoltBankPaymentRequestBodyValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YoltBankUkDomesticInitiateSinglePaymentPreExecutionResultMapper implements UkInitiateSinglePaymentPreExecutionResultMapper<YoltBankUkInitiateSinglePaymentPreExecutionResult> {
    private final YoltBankPaymentRequestBodyValidator validator;

    @Override
    public YoltBankUkInitiateSinglePaymentPreExecutionResult map(InitiateUkDomesticPaymentRequest initiateUkDomesticPaymentRequest) {
        validator.validate(initiateUkDomesticPaymentRequest);
        return new YoltBankUkInitiateSinglePaymentPreExecutionResult(
                initiateUkDomesticPaymentRequest.getRequestDTO(),
                PaymentAuthenticationMeans.fromAuthenticationMeans(initiateUkDomesticPaymentRequest.getAuthenticationMeans()),
                initiateUkDomesticPaymentRequest.getSigner(),
                initiateUkDomesticPaymentRequest.getBaseClientRedirectUrl(),
                initiateUkDomesticPaymentRequest.getState(),
                null,
                null);
    }
}
