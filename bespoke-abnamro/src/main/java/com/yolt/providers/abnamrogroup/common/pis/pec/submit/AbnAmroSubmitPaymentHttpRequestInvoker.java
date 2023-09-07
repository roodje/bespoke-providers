package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroHttpClientFactory;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class AbnAmroSubmitPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<Void, AbnAmroSubmitPaymentPreExecutionResult> {

    private final AbnAmroHttpClientFactory httpClientFactory;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<Void> httpEntity, AbnAmroSubmitPaymentPreExecutionResult preExecutionResult) {
        var httpClient = httpClientFactory.createAbnAmroPisHttpClient(preExecutionResult.getRestTemplateManager(), preExecutionResult.getAuthenticationMeans());
        try {
            return httpClient.submitPayment(httpEntity, preExecutionResult.getTransactionId());
        } catch (TokenInvalidException e) {
            throw new IllegalStateException("Unable to submit payment request", e);
        }
    }
}
