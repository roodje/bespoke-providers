package com.yolt.providers.ing.common.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.http.HttpClientFactory;
import com.yolt.providers.ing.common.pec.PaymentEndpointResolver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.ing.common.pec.IngPecConstants.SUBMIT_PAYMENT_HTTP_METHOD;

@RequiredArgsConstructor
public class DefaultSubmitPaymentHttpRequestInvokerV2 implements PaymentHttpRequestInvoker<Void, DefaultSubmitPaymentPreExecutionResult> {

    private final HttpClientFactory httpClientFactory;
    private final HttpErrorHandler httpErrorHandler;
    private final String providerIdentifier;
    private final PaymentEndpointResolver endpointResolver;

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public ResponseEntity<JsonNode> invokeRequest(final HttpEntity<Void> httpEntity, final DefaultSubmitPaymentPreExecutionResult preExecutionResult) {
        IngAuthenticationMeans authMeans = preExecutionResult.getAuthenticationMeans();
        RestTemplateManager restTemplateManager = preExecutionResult.getRestTemplateManager();

        String expandedEndpointPath = String.format(endpointResolver.getSubmitPaymentEndpoint(preExecutionResult.getPaymentType()),
                preExecutionResult.getPaymentId());

        HttpClient httpClient = httpClientFactory.createPisHttpClient(authMeans, restTemplateManager, providerIdentifier);
        return httpClient.exchange(
                expandedEndpointPath,
                SUBMIT_PAYMENT_HTTP_METHOD,
                httpEntity,
                ProviderClientEndpoints.SUBMIT_PAYMENT,
                JsonNode.class,
                httpErrorHandler);
    }
}
