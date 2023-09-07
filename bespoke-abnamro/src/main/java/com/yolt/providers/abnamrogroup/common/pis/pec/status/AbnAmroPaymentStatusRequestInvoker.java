package com.yolt.providers.abnamrogroup.common.pis.pec.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroHttpClientFactory;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroPisHttpClient;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class AbnAmroPaymentStatusRequestInvoker implements PaymentHttpRequestInvoker<Void, AbnAmroPaymentStatusPreExecutionResult> {

    private final AbnAmroHttpClientFactory httpClientFactory;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<Void> httpEntity, AbnAmroPaymentStatusPreExecutionResult preExecutionResult) {
        AbnAmroPisHttpClient httpClient = httpClientFactory.createAbnAmroPisHttpClient(preExecutionResult.getRestTemplateManager(), preExecutionResult.getAuthenticationMeans());
        return httpClient.getPaymentStatus(httpEntity, preExecutionResult.getProviderState().getTransactionId());
    }
}
