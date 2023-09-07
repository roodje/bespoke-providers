package com.yolt.providers.starlingbank.common.paymentexecutioncontext;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.model.PaymentRequest;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class StarlingBankSubmitPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<PaymentRequest, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> {

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<PaymentRequest> httpEntity, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult) throws PaymentExecutionTechnicalException {

        StarlingBankHttpClient httpClient = preExecutionResult.getHttpClient();
        return httpClient.submitPayment(preExecutionResult.getUrl(), httpEntity);
    }
}
