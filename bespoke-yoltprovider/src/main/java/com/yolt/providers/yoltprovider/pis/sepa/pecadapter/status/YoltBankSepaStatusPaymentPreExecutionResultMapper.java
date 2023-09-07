package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.SepaProviderState;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit.YoltBankSepaSubmitPreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YoltBankSepaStatusPaymentPreExecutionResultMapper implements SepaStatusPaymentPreExecutionResultMapper<YoltBankSepaSubmitPreExecutionResult> {

    private final ObjectMapper objectMapper;

    @Override
    public YoltBankSepaSubmitPreExecutionResult map(GetStatusRequest getStatusRequest) {
        return new YoltBankSepaSubmitPreExecutionResult(
                getStatusRequest.getPaymentId(),
                PaymentAuthenticationMeans.fromAuthenticationMeans(getStatusRequest.getAuthenticationMeans()),
                getStatusRequest.getSigner(),
                retrievePaymentType(getStatusRequest.getProviderState())
        );
    }

    private PaymentType retrievePaymentType(String providerState) {
        try {
            return objectMapper.readValue(providerState, SepaProviderState.class).getPaymentType();
        } catch (Exception e) {
            throw new IllegalStateException("Error when retrieving payment type from provider state during status retrieval");
        }
    }
}
