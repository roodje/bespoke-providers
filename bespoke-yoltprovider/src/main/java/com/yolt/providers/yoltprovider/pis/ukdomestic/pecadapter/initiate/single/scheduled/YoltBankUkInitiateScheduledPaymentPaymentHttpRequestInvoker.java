package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.yoltprovider.pis.ukdomestic.YoltBankUkDomesticHttpService;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticScheduledConsent1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class YoltBankUkInitiateScheduledPaymentPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<OBWriteDomesticScheduledConsent1, YoltBankUkInitiateScheduledPaymentPreExecutionResult> {

    private final YoltBankUkDomesticHttpService httpService;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<OBWriteDomesticScheduledConsent1> httpEntity, YoltBankUkInitiateScheduledPaymentPreExecutionResult ybUkInitiateSinglePaymentPreExecutionResult) {
        return httpService.postInitiateScheduledPayment(httpEntity);
    }
}
