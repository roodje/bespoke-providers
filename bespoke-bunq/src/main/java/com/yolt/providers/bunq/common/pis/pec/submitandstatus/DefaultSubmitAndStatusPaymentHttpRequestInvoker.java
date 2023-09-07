package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.bunq.common.pis.pec.DefaultEndpointUrlProvider;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class DefaultSubmitAndStatusPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<Void, DefaultSubmitAndStatusPaymentPreExecutionResult> {

    private final DefaultEndpointUrlProvider urlProvider;

    @Override
    @SneakyThrows
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<Void> httpEntity, DefaultSubmitAndStatusPaymentPreExecutionResult preExecutionResult) {
        var httpClient = preExecutionResult.getHttpClient();
        return httpClient.getPaymentStatus(httpEntity, urlProvider.getStatusDraftPaymentUrl(preExecutionResult.getPsd2UserId(), preExecutionResult.getPaymentId()));
    }
}
