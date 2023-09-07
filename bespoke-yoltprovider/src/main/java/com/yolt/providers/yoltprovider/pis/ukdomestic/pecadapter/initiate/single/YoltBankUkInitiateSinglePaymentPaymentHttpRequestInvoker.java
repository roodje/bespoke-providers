package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.yoltprovider.pis.ukdomestic.YoltBankUkDomesticHttpService;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticConsent1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class YoltBankUkInitiateSinglePaymentPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<OBWriteDomesticConsent1, YoltBankUkInitiateSinglePaymentPreExecutionResult> {

    private final YoltBankUkDomesticHttpService httpService;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<OBWriteDomesticConsent1> httpEntity, YoltBankUkInitiateSinglePaymentPreExecutionResult yoltBankUkInitiateSinglePaymentPreExecutionResult) {
        return httpService.postInitiateSinglePayment(httpEntity);
    }
}
