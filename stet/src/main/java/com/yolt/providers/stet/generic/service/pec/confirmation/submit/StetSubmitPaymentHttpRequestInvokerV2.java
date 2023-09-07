package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.SUBMIT_PAYMENT;

@RequiredArgsConstructor
public class StetSubmitPaymentHttpRequestInvokerV2 implements PaymentHttpRequestInvoker<StetPaymentConfirmationRequestDTO, StetConfirmationPreExecutionResult> {

    private final HttpErrorHandlerV2 errorHandler;

    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<StetPaymentConfirmationRequestDTO> httpEntity,
                                                  StetConfirmationPreExecutionResult preExecutionResult) {

        HttpClient httpClient = preExecutionResult.getHttpClient();

        return httpClient.exchange(
                preExecutionResult.getRequestPath(),
                preExecutionResult.getHttpMethod(),
                httpEntity,
                SUBMIT_PAYMENT,
                JsonNode.class,
                errorHandler);
    }
}
