package com.yolt.providers.starlingbank.common.paymentexecutioncontext;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class StarlingBankStatusPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<Object, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> {

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public ResponseEntity<JsonNode> invokeRequest(final HttpEntity<Object> httpEntity, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult) throws PaymentExecutionTechnicalException {

        StarlingBankHttpClient httpClient = preExecutionResult.getHttpClient();
        return httpClient.paymentStatus(preExecutionResult.getUrl(), preExecutionResult.getToken());
    }
}
