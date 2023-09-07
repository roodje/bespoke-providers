package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.common.pis.InitiatePaymentResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateSerializer;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AbnAmroInitiatePaymentProviderStateExtractor implements SepaPaymentProviderStateExtractor<InitiatePaymentResponseDTO, AbnAmroInitiatePaymentPreExecutionResult> {

    private final AbnAmroProviderStateSerializer providerStateSerializer;

    @Override
    public String extractProviderState(InitiatePaymentResponseDTO initiatePaymentResponseDTO, AbnAmroInitiatePaymentPreExecutionResult preExecutionResult) {
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState();
        providerState.setTransactionId(initiatePaymentResponseDTO.getTransactionId());
        providerState.setRedirectUri(preExecutionResult.getBaseClientRedirectUrl());
        return providerStateSerializer.serialize(providerState);
    }
}
