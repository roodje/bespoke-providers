package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.yoltprovider.pis.ukdomestic.YoltBankUkDomesticHttpService;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsent1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class YoltBankUkInitiatePeriodicPaymentPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<OBWriteDomesticStandingOrderConsent1, YoltBankUkInitiatePeriodicPaymentPreExecutionResult> {

    private final YoltBankUkDomesticHttpService httpService;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<OBWriteDomesticStandingOrderConsent1> httpEntity, YoltBankUkInitiatePeriodicPaymentPreExecutionResult ybUkInitiateSinglePaymentPreExecutionResult) {
        return httpService.postInitiatePeriodicPayment(httpEntity);
    }
}
