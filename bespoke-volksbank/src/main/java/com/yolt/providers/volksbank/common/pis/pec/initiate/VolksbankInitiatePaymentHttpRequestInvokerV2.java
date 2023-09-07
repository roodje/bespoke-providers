package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientFactoryV2;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class VolksbankInitiatePaymentHttpRequestInvokerV2 implements PaymentHttpRequestInvoker<InitiatePaymentRequest, VolksbankSepaInitiatePreExecutionResult> {

    private final VolksbankHttpClientFactoryV2 httpClientFactory;
    private final ProviderIdentification providerIdentification;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<InitiatePaymentRequest> httpEntity, VolksbankSepaInitiatePreExecutionResult preExecutionResult) {
        var httpClient = httpClientFactory.createPisHttpClient(preExecutionResult.getAuthenticationMeans(),
                preExecutionResult.getRestTemplateManager(),
                providerIdentification.getProviderDisplayName());
        return httpClient.initiatePayment(httpEntity);
    }
}
