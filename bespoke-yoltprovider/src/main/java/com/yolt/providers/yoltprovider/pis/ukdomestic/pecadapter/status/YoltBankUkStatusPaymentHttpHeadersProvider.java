package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.YoltBankUkSubmitPreExecutionResult;
import org.springframework.http.HttpHeaders;

public class YoltBankUkStatusPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<YoltBankUkSubmitPreExecutionResult, Void> {

    private static final String CLIENT_ID = "client-id";

    @Override
    public HttpHeaders provideHttpHeaders(YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult, Void unused) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID, yoltBankUkSubmitPreExecutionResult.getClientId().toString());
        return httpHeaders;
    }
}
