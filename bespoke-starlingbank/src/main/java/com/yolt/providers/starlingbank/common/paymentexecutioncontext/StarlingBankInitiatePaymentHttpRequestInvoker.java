package com.yolt.providers.starlingbank.common.paymentexecutioncontext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankInitiatePaymentExecutionContextPreExecutionResult;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public class StarlingBankInitiatePaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<String, StarlingBankInitiatePaymentExecutionContextPreExecutionResult> {

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<String> httpEntity, StarlingBankInitiatePaymentExecutionContextPreExecutionResult starlingBankInitiatePaymentExecutionContextPreExecutionResult) throws PaymentExecutionTechnicalException {
        return ResponseEntity.ok(new TextNode(""));
    }
}
