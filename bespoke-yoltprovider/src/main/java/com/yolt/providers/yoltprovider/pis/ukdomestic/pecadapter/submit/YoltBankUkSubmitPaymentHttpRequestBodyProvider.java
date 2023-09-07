package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.yoltprovider.pis.ukdomestic.ConfirmPaymentRequest;

public class YoltBankUkSubmitPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<YoltBankUkSubmitPreExecutionResult, ConfirmPaymentRequest> {

    @Override
    public ConfirmPaymentRequest provideHttpRequestBody(YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult) {
        return new ConfirmPaymentRequest(yoltBankUkSubmitPreExecutionResult.getPaymentId());
    }
}