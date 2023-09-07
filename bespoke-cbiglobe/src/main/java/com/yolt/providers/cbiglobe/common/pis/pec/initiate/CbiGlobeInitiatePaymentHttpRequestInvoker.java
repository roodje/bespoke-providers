package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import com.yolt.providers.cbiglobe.common.rest.CbiGlobePisHttpClientFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class CbiGlobeInitiatePaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<InitiatePaymentRequest, CbiGlobeSepaInitiatePreExecutionResult> {

    private final CbiGlobePisHttpClientFactory httpClientFactory;
    private final ProviderIdentification providerIdentification;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<InitiatePaymentRequest> httpEntity, CbiGlobeSepaInitiatePreExecutionResult preExecutionResult) {
        var httpClient = httpClientFactory.createPisHttpClient(preExecutionResult.getAuthenticationMeans(),
                preExecutionResult.getRestTemplateManager(),
                providerIdentification.getProviderDisplayName());
        return httpClient.initiatePayment(httpEntity);
    }
}
