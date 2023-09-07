package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.single;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.SepaProviderState;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.SepaInitiatePaymentResponse;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common.YoltBankSepaInitiatePaymentPreExecutionResult;

public class YoltBankSepaSinglePaymentProviderStateExtractor implements SepaPaymentProviderStateExtractor<SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult> {

    private final ObjectMapper objectMapper;

    public YoltBankSepaSinglePaymentProviderStateExtractor(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String extractProviderState(SepaInitiatePaymentResponse responseDTO, YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult) {
        try {
            return objectMapper.writeValueAsString(new SepaProviderState(responseDTO.getPaymentId(), PaymentType.SINGLE));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
