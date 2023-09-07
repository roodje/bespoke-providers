package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.yoltprovider.pis.ukdomestic.ConfirmPaymentRequest;
import org.springframework.http.HttpHeaders;

public class YoltBankUkSubmitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<YoltBankUkSubmitPreExecutionResult, ConfirmPaymentRequest> {

    private static final String CLIENT_ID = "client-id";

    @Override
    public HttpHeaders provideHttpHeaders(YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult, ConfirmPaymentRequest confirmPaymentRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID, yoltBankUkSubmitPreExecutionResult.getClientId().toString());
        return httpHeaders;
    }
}
