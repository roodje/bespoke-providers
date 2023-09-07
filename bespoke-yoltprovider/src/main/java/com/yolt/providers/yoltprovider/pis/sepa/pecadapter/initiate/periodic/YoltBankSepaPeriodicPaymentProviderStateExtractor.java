package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.periodic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentResponseDTO;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.SepaProviderState;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.SepaInitiatePaymentResponse;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common.YoltBankSepaInitiatePaymentPreExecutionResult;

public class YoltBankSepaPeriodicPaymentProviderStateExtractor implements SepaPaymentProviderStateExtractor<SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult> {

    private final ObjectMapper objectMapper;

    public YoltBankSepaPeriodicPaymentProviderStateExtractor(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String extractProviderState(SepaInitiatePaymentResponse responseDTO, YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult) {
        try {
            return objectMapper.writeValueAsString(new SepaProviderState(responseDTO.getPaymentId(), PaymentType.PERIODIC));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
