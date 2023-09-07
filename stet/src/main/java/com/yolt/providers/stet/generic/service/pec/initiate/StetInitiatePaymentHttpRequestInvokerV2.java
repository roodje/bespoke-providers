package com.yolt.providers.stet.generic.service.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.INITIATE_PAYMENT;

@RequiredArgsConstructor
public class StetInitiatePaymentHttpRequestInvokerV2 implements PaymentHttpRequestInvoker<StetPaymentInitiationRequestDTO, StetInitiatePreExecutionResult> {

    private final HttpErrorHandlerV2 errorHandler;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<StetPaymentInitiationRequestDTO> httpEntity,
                                                  StetInitiatePreExecutionResult preExecutionResult) {

        HttpClient httpClient = preExecutionResult.getHttpClient();

        return httpClient.exchange(
                preExecutionResult.getRequestPath(),
                preExecutionResult.getHttpMethod(),
                httpEntity,
                INITIATE_PAYMENT,
                JsonNode.class,
                errorHandler);
    }
}
