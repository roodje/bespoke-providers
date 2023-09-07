package com.yolt.providers.stet.generic.service.pec.confirmation;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.confirmation.status.StetStatusPaymentPreExecutionResultMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StetConfirmationPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> implements SepaSubmitPaymentPreExecutionResultMapper<StetConfirmationPreExecutionResult> {

    private final StetStatusPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> statusPaymentPreExecutionResultMapper;
    private final ProviderStateMapper providerStateMapper;

    @Override
    public StetConfirmationPreExecutionResult map(SubmitPaymentRequest request) {
        String providerState = request.getProviderState();
        PaymentProviderState paymentProviderState = providerStateMapper.mapToPaymentProviderState(providerState);

        GetStatusRequest statusRequest = new GetStatusRequest(
                providerState,
                paymentProviderState.getPaymentId(),
                request.getAuthenticationMeans(),
                request.getSigner(),
                request.getRestTemplateManager(),
                request.getPsuIpAddress(),
                request.getAuthenticationMeansReference());

        return statusPaymentPreExecutionResultMapper.map(statusRequest);
    }
}
