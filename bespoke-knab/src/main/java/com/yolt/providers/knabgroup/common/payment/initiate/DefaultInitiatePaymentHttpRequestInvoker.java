package com.yolt.providers.knabgroup.common.payment.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClientFactory;
import com.yolt.providers.knabgroup.common.payment.PaymentEndpointResolver;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentRequestBody;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class DefaultInitiatePaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<InitiatePaymentRequestBody, InitiatePaymentPreExecutionResult> {

    private final KnabGroupHttpClientFactory httpClientFactory;
    private final HttpErrorHandler httpErrorHandler;
    private final PaymentType paymentType;
    private final PaymentEndpointResolver endpointResolver;

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public ResponseEntity<JsonNode> invokeRequest(final HttpEntity<InitiatePaymentRequestBody> httpEntity, final InitiatePaymentPreExecutionResult preExecutionResult) {
        KnabGroupAuthenticationMeans authMeans = preExecutionResult.getAuthenticationMeans();
        RestTemplateManager restTemplateManager = preExecutionResult.getRestTemplateManager();

        HttpClient httpClient = httpClientFactory.createKnabGroupHttpClient(restTemplateManager, authMeans);
        return httpClient.exchange(
                endpointResolver.getInitiatePaymentEndpoint(paymentType),
                HttpMethod.POST,
                httpEntity,
                ProviderClientEndpoints.INITIATE_PAYMENT,
                JsonNode.class,
                httpErrorHandler
        );
    }
}
