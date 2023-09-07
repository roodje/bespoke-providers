package com.yolt.providers.knabgroup.common.payment.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClientFactory;
import com.yolt.providers.knabgroup.common.payment.PaymentEndpointResolver;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class DefaultStatusPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<Void, StatusPaymentPreExecutionResult> {

    private final KnabGroupHttpClientFactory httpClientFactory;
    private final HttpErrorHandler httpErrorHandler;
    private final PaymentEndpointResolver endpointResolver;

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public ResponseEntity<JsonNode> invokeRequest(final HttpEntity<Void> httpEntity, final StatusPaymentPreExecutionResult preExecutionResult) {
        KnabGroupAuthenticationMeans authMeans = preExecutionResult.getAuthenticationMeans();
        RestTemplateManager restTemplateManager = preExecutionResult.getRestTemplateManager();
        HttpClient httpClient = httpClientFactory.createKnabGroupHttpClient(restTemplateManager, authMeans);
        return httpClient.exchange(
                endpointResolver.getStatusPaymentEndpoint(preExecutionResult.getPaymentType()),
                HttpMethod.GET,
                httpEntity,
                ProviderClientEndpoints.GET_PAYMENT_STATUS,
                JsonNode.class,
                httpErrorHandler,
                preExecutionResult.getPaymentId());
    }
}
