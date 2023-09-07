package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentRequest;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_PAYMENT_STATUS;

@Slf4j
@RequiredArgsConstructor
@Deprecated
public class StetStatusPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<StetPaymentConfirmationRequestDTO, StetConfirmationPreExecutionResult> {

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

        try {
            return httpClient.exchange(
                    preExecutionResult.getRequestPath(),
                    preExecutionResult.getHttpMethod(),
                    httpEntity,
                    GET_PAYMENT_STATUS,
                    JsonNode.class,
                    errorHandler);
        } catch (Exception e) {
            log.info("Get payment status failed due to: {}", e.getMessage());
            final var paymentRequest = new StetPaymentRequest();
            paymentRequest.setPaymentInformationStatus(StetPaymentStatus.RJCT);
            final var stetPaymentStatusResponseDTO = new StetPaymentStatusResponseDTO();
            stetPaymentStatusResponseDTO.setPaymentRequest(paymentRequest);
            return ResponseEntity.ok(JsonNodeFactory.instance.pojoNode(stetPaymentStatusResponseDTO));
        }
    }
}
