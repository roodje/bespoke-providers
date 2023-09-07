package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateSerializer;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import lombok.RequiredArgsConstructor;

import java.time.Clock;

@RequiredArgsConstructor
public class AbnAmroSubmitPaymentProviderStateExtractor implements SepaPaymentProviderStateExtractor<TransactionStatusResponse, AbnAmroSubmitPaymentPreExecutionResult> {

    private final AbnAmroProviderStateSerializer providerStateSerializer;
    private final Clock clock;

    @Override
    public String extractProviderState(TransactionStatusResponse transactionStatusResponse, AbnAmroSubmitPaymentPreExecutionResult preExecutionResult) {
        AccessTokenResponseDTO accessTokenResponseDTO = preExecutionResult.getAccessTokenResponseDTO();
        AbnAmroPaymentProviderState.UserAccessTokenState userAccessTokenState = new AbnAmroPaymentProviderState.UserAccessTokenState(accessTokenResponseDTO.getAccessToken(),
                accessTokenResponseDTO.getRefreshToken(),
                accessTokenResponseDTO.getExpiresIn(),
                clock);
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState();
        providerState.setTransactionId(preExecutionResult.getTransactionId());
        providerState.setUserAccessTokenState(userAccessTokenState);
        return providerStateSerializer.serialize(providerState);
    }
}
