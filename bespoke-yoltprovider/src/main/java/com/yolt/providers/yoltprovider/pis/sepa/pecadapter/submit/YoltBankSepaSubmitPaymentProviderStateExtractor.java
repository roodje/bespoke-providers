package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.SepaProviderState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YoltBankSepaSubmitPaymentProviderStateExtractor implements SepaPaymentProviderStateExtractor<SepaPaymentStatusResponse, YoltBankSepaSubmitPreExecutionResult> {

    private final ObjectMapper objectMapper;

    @Override
    public String extractProviderState(SepaPaymentStatusResponse responseDTO,
                                       YoltBankSepaSubmitPreExecutionResult preExecutionResult) {
        try {
            return objectMapper.writeValueAsString(new SepaProviderState(preExecutionResult.getPaymentId(), preExecutionResult.getPaymentType()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
