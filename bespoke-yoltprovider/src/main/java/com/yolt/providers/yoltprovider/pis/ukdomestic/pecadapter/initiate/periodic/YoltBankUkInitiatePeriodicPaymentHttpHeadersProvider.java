package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsent1;
import org.springframework.http.HttpHeaders;

public class YoltBankUkInitiatePeriodicPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<YoltBankUkInitiatePeriodicPaymentPreExecutionResult, OBWriteDomesticStandingOrderConsent1> {

    private static final String CLIENT_ID = "client-id";

    @Override
    public HttpHeaders provideHttpHeaders(YoltBankUkInitiatePeriodicPaymentPreExecutionResult result, OBWriteDomesticStandingOrderConsent1 consent) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID, result.getAuthenticationMeans().getClientId().toString());
        return httpHeaders;
    }
}
