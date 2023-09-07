package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticScheduledConsent1;
import org.springframework.http.HttpHeaders;

public class YoltBankUkInitiateScheduledPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<YoltBankUkInitiateScheduledPaymentPreExecutionResult, OBWriteDomesticScheduledConsent1> {

    private static final String CLIENT_ID = "client-id";

    @Override
    public HttpHeaders provideHttpHeaders(YoltBankUkInitiateScheduledPaymentPreExecutionResult result, OBWriteDomesticScheduledConsent1 consent) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID, result.getAuthenticationMeans().getClientId().toString());
        return httpHeaders;
    }
}
