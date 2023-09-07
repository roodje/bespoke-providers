package com.yolt.providers.fineco.pis.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.fineco.config.FinecoProperties;
import com.yolt.providers.fineco.dto.PaymentRequest;
import com.yolt.providers.fineco.rest.FinecoHttpClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class FinecoInitiatePaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<PaymentRequest, FinecoInitiatePaymentPreExecutionResult> {

    private final FinecoHttpClientFactory httpClientFactory;
    private final HttpErrorHandlerV2 httpErrorHandler;
    private final String providerIdentifier;
    private final FinecoProperties properties;

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<PaymentRequest> httpEntity, FinecoInitiatePaymentPreExecutionResult preExecutionResult) {
        var authMeans = preExecutionResult.getAuthenticationMeans();
        var restTemplateManager = preExecutionResult.getRestTemplateManager();

        var httpClient = httpClientFactory.createHttpClient(authMeans, restTemplateManager, providerIdentifier);

        return httpClient.exchange(
                properties.getSepaPayments().getPaymentInitiation(),
                HttpMethod.POST,
                httpEntity,
                ProviderClientEndpoints.INITIATE_PAYMENT,
                JsonNode.class,
                httpErrorHandler
        );
    }
}
