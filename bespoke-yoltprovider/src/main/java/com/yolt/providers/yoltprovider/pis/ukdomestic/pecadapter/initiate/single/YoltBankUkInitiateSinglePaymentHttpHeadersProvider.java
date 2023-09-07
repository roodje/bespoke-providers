package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticConsent1;
import org.springframework.http.HttpHeaders;

public class YoltBankUkInitiateSinglePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<YoltBankUkInitiateSinglePaymentPreExecutionResult, OBWriteDomesticConsent1> {

    private static final String CLIENT_ID = "client-id";

    @Override
    public HttpHeaders provideHttpHeaders(YoltBankUkInitiateSinglePaymentPreExecutionResult result, OBWriteDomesticConsent1 consent) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID, result.getAuthenticationMeans().getClientId().toString());
        return httpHeaders;
    }
}
