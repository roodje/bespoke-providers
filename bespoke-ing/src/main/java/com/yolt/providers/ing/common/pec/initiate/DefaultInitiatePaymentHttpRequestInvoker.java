package com.yolt.providers.ing.common.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import com.yolt.providers.ing.common.http.HttpClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.ing.common.pec.IngPecConstants.INITIATE_PAYMENT_ENDPOINT;
import static com.yolt.providers.ing.common.pec.IngPecConstants.INITIATE_PAYMENT_HTTP_METHOD;

/**
 * {@inheritDoc}
 *
 * @deprecated Use {@link DefaultInitiatePaymentHttpRequestInvokerV2} instead
 */
@RequiredArgsConstructor
public class DefaultInitiatePaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<SepaCreditTransfer, DefaultInitiatePaymentPreExecutionResult> {

    private final HttpClientFactory httpClientFactory;
    private final HttpErrorHandler httpErrorHandler;
    private final String providerIdentifier;

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public ResponseEntity<JsonNode> invokeRequest(final HttpEntity<SepaCreditTransfer> httpEntity, final DefaultInitiatePaymentPreExecutionResult preExecutionResult) {
        IngAuthenticationMeans authMeans = preExecutionResult.getAuthenticationMeans();
        RestTemplateManager restTemplateManager = preExecutionResult.getRestTemplateManager();

        HttpClient httpClient = httpClientFactory.createPisHttpClient(authMeans, restTemplateManager, providerIdentifier);
        return httpClient.exchange(
                INITIATE_PAYMENT_ENDPOINT,
                INITIATE_PAYMENT_HTTP_METHOD,
                httpEntity,
                ProviderClientEndpoints.INITIATE_PAYMENT,
                JsonNode.class,
                httpErrorHandler
        );
    }
}
