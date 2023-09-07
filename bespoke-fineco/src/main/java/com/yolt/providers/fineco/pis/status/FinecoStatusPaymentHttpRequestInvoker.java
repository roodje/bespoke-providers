package com.yolt.providers.fineco.pis.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.fineco.config.FinecoProperties;
import com.yolt.providers.fineco.rest.FinecoHttpClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class FinecoStatusPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<Void, FinecoStatusPaymentPreExecutionResult> {

    private final FinecoHttpClientFactory httpClientFactory;
    private final HttpErrorHandlerV2 httpErrorHandler;
    private final String providerIdentifier;
    private final FinecoProperties properties;

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public ResponseEntity<JsonNode> invokeRequest(final HttpEntity<Void> httpEntity, FinecoStatusPaymentPreExecutionResult preExecutionResult) {
        var authMeans = preExecutionResult.getAuthenticationMeans();
        var restTemplateManager = preExecutionResult.getRestTemplateManager();
        var httpClient = httpClientFactory.createHttpClient(authMeans, restTemplateManager, providerIdentifier);
        var paymentsUrl = String.format(properties.getSepaPayments().getPaymentStatus(), preExecutionResult.getPaymentId());

        return httpClient.exchange(
                paymentsUrl,
                HttpMethod.GET,
                httpEntity,
                ProviderClientEndpoints.GET_PAYMENT_STATUS,
                JsonNode.class,
                httpErrorHandler);
    }
}
