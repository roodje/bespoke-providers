package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroHttpClientFactory;
import com.yolt.providers.abnamro.pis.SepaPayment;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class AbnAmroInitiatePaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<SepaPayment, AbnAmroInitiatePaymentPreExecutionResult> {

    private final AbnAmroHttpClientFactory httpClientFactory;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<SepaPayment> httpEntity, AbnAmroInitiatePaymentPreExecutionResult preExecutionResult) {
        var httpClient = httpClientFactory.createAbnAmroPisHttpClient(preExecutionResult.getRestTemplateManager(), preExecutionResult.getAuthenticationMeans());
        return httpClient.initiatePayment(httpEntity);
    }
}
