package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;

public class YoltBankSepaInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<YoltBankSepaInitiatePaymentPreExecutionResult, byte[]> {

    private final ObjectMapper objectMapper;

    public YoltBankSepaInitiatePaymentHttpRequestBodyProvider(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] provideHttpRequestBody(YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult) {
        try {
            return objectMapper.writeValueAsBytes(preExecutionResult.getRequestDTO());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
