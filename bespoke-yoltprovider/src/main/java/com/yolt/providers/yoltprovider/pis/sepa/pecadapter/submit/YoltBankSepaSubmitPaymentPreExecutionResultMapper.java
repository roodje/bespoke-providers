package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.SepaProviderState;

public class YoltBankSepaSubmitPaymentPreExecutionResultMapper implements SepaSubmitPaymentPreExecutionResultMapper<YoltBankSepaSubmitPreExecutionResult> {

    private final ObjectMapper objectMapper;

    public YoltBankSepaSubmitPaymentPreExecutionResultMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public YoltBankSepaSubmitPreExecutionResult map(SubmitPaymentRequest submitPaymentRequest) {
        SepaProviderState providerState = readState(submitPaymentRequest);
        return new YoltBankSepaSubmitPreExecutionResult(
                providerState.getPaymentId(),
                PaymentAuthenticationMeans.fromAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans()),
                submitPaymentRequest.getSigner(),
                providerState.getPaymentType()

        );
    }

    private SepaProviderState readState(final SubmitPaymentRequest submitPaymentRequest) {
        try {
            return objectMapper.readValue(submitPaymentRequest.getProviderState(), SepaProviderState.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not find payment type in providerState during payment submission");
        }
    }
}
