package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VolksbankInitiatePaymentPreExecutionResultMapperV2 implements SepaInitiateSinglePaymentPreExecutionResultMapper<VolksbankSepaInitiatePreExecutionResult> {

    private final ProviderIdentification providerIdentification;

    @Override
    public VolksbankSepaInitiatePreExecutionResult map(InitiatePaymentRequest initiatePaymentRequest) {
        return VolksbankSepaInitiatePreExecutionResult.builder()
                .requestDTO(initiatePaymentRequest.getRequestDTO())
                .authenticationMeans(VolksbankAuthenticationMeans.fromAuthenticationMeans(initiatePaymentRequest.getAuthenticationMeans(), providerIdentification.getProviderIdentifier()))
                .restTemplateManager(initiatePaymentRequest.getRestTemplateManager())
                .psuIpAddress(initiatePaymentRequest.getPsuIpAddress())
                .state(initiatePaymentRequest.getState())
                .baseClientRedirectUrl(initiatePaymentRequest.getBaseClientRedirectUrl())
                .build();
    }
}
