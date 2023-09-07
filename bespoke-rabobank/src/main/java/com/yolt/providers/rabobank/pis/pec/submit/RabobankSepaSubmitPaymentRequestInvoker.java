package com.yolt.providers.rabobank.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHttpClient;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHttpClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class RabobankSepaSubmitPaymentRequestInvoker implements PaymentHttpRequestInvoker<Void, RabobankSepaSubmitPaymentPreExecutionResult> {

    private final RabobankPisHttpClientFactory httpClientFactory;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<Void> httpEntity, RabobankSepaSubmitPaymentPreExecutionResult preExecutionResult) {
        RabobankPisHttpClient httpClient = httpClientFactory.createRabobankPisHttpClient(preExecutionResult.getRestTemplateManager(), preExecutionResult.getAuthenticationMeans());
        return httpClient.getStatus(httpEntity, preExecutionResult.getPaymentId());
    }
}
