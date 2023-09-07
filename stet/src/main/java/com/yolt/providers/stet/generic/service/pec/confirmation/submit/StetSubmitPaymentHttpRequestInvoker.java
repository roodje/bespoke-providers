package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.SUBMIT_PAYMENT;

@RequiredArgsConstructor
@Deprecated
public class StetSubmitPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<StetPaymentConfirmationRequestDTO, StetConfirmationPreExecutionResult> {

    private final HttpClientFactory httpClientFactory;
    private final ProviderIdentification providerIdentification;
    private final HttpErrorHandler errorHandler;
    private final DefaultProperties properties;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<StetPaymentConfirmationRequestDTO> httpEntity,
                                                  StetConfirmationPreExecutionResult preExecutionResult) {
        Region region = properties.getRegions().get(0);

        HttpClient httpClient = httpClientFactory.createHttpClient(
                preExecutionResult.getRestTemplateManager(),
                preExecutionResult.getAuthMeans(),
                region.getBaseUrl(),
                providerIdentification.getDisplayName());

        return httpClient.exchange(
                preExecutionResult.getRequestPath(),
                preExecutionResult.getHttpMethod(),
                httpEntity,
                SUBMIT_PAYMENT,
                JsonNode.class,
                errorHandler);
    }
}
