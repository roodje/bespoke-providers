package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.rest.CbiGlobePisHttpClientFactory;
import com.yolt.providers.cbiglobe.pis.dto.GetPaymentStatusRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@Slf4j
@RequiredArgsConstructor
public class CbiGlobeSubmitPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<Void, CbiGlobeSepaSubmitPreExecutionResult> {

    private final CbiGlobePisHttpClientFactory httpClientFactory;
    private final ProviderIdentification providerIdentification;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<Void> httpEntity, CbiGlobeSepaSubmitPreExecutionResult preExecutionResult) {
        var pisHttpClient = httpClientFactory.createPisHttpClient(preExecutionResult.getAuthenticationMeans(),
                preExecutionResult.getRestTemplateManager(),
                providerIdentification.getProviderDisplayName());
        try {
            return pisHttpClient.getPaymentStatus(httpEntity, preExecutionResult.getPaymentId());
        } catch (Exception e) {
            log.info("Get payment status failed due to: {}", e.getMessage());
            var getPaymentStatusRequestResponseType = new GetPaymentStatusRequestResponseType();
            getPaymentStatusRequestResponseType.setTransactionStatus("RJCT");
            return ResponseEntity.ok(JsonNodeFactory.instance.pojoNode(getPaymentStatusRequestResponseType));
        }
    }
}
