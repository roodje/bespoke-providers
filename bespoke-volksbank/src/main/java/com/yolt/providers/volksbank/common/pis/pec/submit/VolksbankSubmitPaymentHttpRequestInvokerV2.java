package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientFactoryV2;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class VolksbankSubmitPaymentHttpRequestInvokerV2 implements PaymentHttpRequestInvoker<Void, VolksbankSepaSubmitPreExecutionResult> {

    private final VolksbankHttpClientFactoryV2 httpClientFactory;
    private final ProviderIdentification providerIdentification;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<Void> httpEntity, VolksbankSepaSubmitPreExecutionResult preExecutionResult) {
        var pisHttpClient = httpClientFactory.createPisHttpClient(preExecutionResult.getAuthenticationMeans(),
                preExecutionResult.getRestTemplateManager(),
                providerIdentification.getProviderDisplayName());
        return pisHttpClient.getPaymentStatus(httpEntity, preExecutionResult.getPaymentId());
    }
}
