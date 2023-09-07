package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkDomesticStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.YoltBankUkSubmitPreExecutionResult;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class YoltBankUkDomesticStatusPaymentPreExecutionResultMapper implements UkDomesticStatusPaymentPreExecutionResultMapper<YoltBankUkSubmitPreExecutionResult> {

    private final ObjectMapper objectMapper;

    @Override
    public YoltBankUkSubmitPreExecutionResult map(GetStatusRequest getStatusRequest) {
        return new YoltBankUkSubmitPreExecutionResult(
                UUID.fromString(getStatusRequest.getPaymentId()),
                PaymentAuthenticationMeans.fromAuthenticationMeans(getStatusRequest.getAuthenticationMeans()).getClientId(),
                getPaymentType(getStatusRequest.getProviderState())
        );
    }

    private PaymentType getPaymentType(String providerState) {
        try {
            return objectMapper.readValue(providerState, UkProviderState.class).getPaymentType();
        } catch (Exception e) {
            throw new IllegalStateException("Error when retrieving payment type from provider state during status retrieval");
        }
    }
}
