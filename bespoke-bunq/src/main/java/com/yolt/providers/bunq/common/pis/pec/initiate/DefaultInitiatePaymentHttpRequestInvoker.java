package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentRequest;
import com.yolt.providers.bunq.common.pis.pec.DefaultEndpointUrlProvider;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class DefaultInitiatePaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<PaymentServiceProviderDraftPaymentRequest, DefaultInitiatePaymentPreExecutionResult> {

    private final DefaultEndpointUrlProvider urlProvider;

    @Override
    @SneakyThrows
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<PaymentServiceProviderDraftPaymentRequest> httpEntity, DefaultInitiatePaymentPreExecutionResult preExecutionResult) {
        var httpClient = preExecutionResult.getHttpClient();
        return httpClient.createPayment(httpEntity, urlProvider.getInitiateDraftPaymentUrl(preExecutionResult.getPsd2UserId()));
    }
}
