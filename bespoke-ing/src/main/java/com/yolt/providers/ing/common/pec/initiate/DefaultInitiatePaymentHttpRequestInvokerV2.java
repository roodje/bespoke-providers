package com.yolt.providers.ing.common.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import com.yolt.providers.ing.common.http.HttpClientFactory;
import com.yolt.providers.ing.common.pec.PaymentEndpointResolver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.ing.common.pec.IngPecConstants.INITIATE_PAYMENT_HTTP_METHOD;

@RequiredArgsConstructor
public class DefaultInitiatePaymentHttpRequestInvokerV2 implements PaymentHttpRequestInvoker<SepaCreditTransfer, DefaultInitiatePaymentPreExecutionResult> {

    private final HttpClientFactory httpClientFactory;
    private final HttpErrorHandler httpErrorHandler;
    private final String providerIdentifier;
    private final PaymentType paymentType;
    private final PaymentEndpointResolver endpointResolver;

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public ResponseEntity<JsonNode> invokeRequest(final HttpEntity<SepaCreditTransfer> httpEntity, final DefaultInitiatePaymentPreExecutionResult preExecutionResult) {
        IngAuthenticationMeans authMeans = preExecutionResult.getAuthenticationMeans();
        RestTemplateManager restTemplateManager = preExecutionResult.getRestTemplateManager();

        HttpClient httpClient = httpClientFactory.createPisHttpClient(authMeans, restTemplateManager, providerIdentifier);
        return httpClient.exchange(
                endpointResolver.getInitiatePaymentEndpoint(paymentType),
                INITIATE_PAYMENT_HTTP_METHOD,
                httpEntity,
                ProviderClientEndpoints.INITIATE_PAYMENT,
                JsonNode.class,
                httpErrorHandler
        );
    }
}
