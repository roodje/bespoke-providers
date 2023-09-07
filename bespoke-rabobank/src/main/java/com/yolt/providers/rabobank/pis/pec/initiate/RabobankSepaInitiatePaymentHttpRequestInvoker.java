package com.yolt.providers.rabobank.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.rabobank.dto.external.SepaCreditTransfer;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHttpClient;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHttpClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class RabobankSepaInitiatePaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<SepaCreditTransfer, RabobankSepaInitiatePreExecutionResult> {

    private final RabobankPisHttpClientFactory httpClientFactory;

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<SepaCreditTransfer> httpEntity, RabobankSepaInitiatePreExecutionResult preExecutionResult) {
        RabobankPisHttpClient httpClient = httpClientFactory.createRabobankPisHttpClient(preExecutionResult.getRestTemplateManager(), preExecutionResult.getAuthenticationMeans());
        return httpClient.initiatePayment(httpEntity);
    }
}
