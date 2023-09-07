package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.periodic;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.yoltprovider.pis.sepa.YoltBankSepaPaymentHttpService;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common.YoltBankSepaInitiatePaymentPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class YoltBankSepaInitiatePeriodicPaymentPaymentHttpInvoker implements PaymentHttpRequestInvoker<byte[], YoltBankSepaInitiatePaymentPreExecutionResult> {

    private final YoltBankSepaPaymentHttpService httpService;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<byte[]> httpEntity, YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult) {
        return httpService.postInitiatePeriodicPaymentRequest(httpEntity);
    }
}
