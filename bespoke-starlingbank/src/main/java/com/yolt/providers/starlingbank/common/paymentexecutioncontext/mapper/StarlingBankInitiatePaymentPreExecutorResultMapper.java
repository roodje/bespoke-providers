package com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkDomesticInitiatePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.starlingbank.common.model.UkDomesticPaymentProviderState;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankInitiatePaymentExecutionContextPreExecutionResult;

import java.util.UUID;

public class StarlingBankInitiatePaymentPreExecutorResultMapper implements UkDomesticInitiatePaymentPreExecutionResultMapper<StarlingBankInitiatePaymentExecutionContextPreExecutionResult> {

    @Override
    public StarlingBankInitiatePaymentExecutionContextPreExecutionResult map(InitiateUkDomesticPaymentRequest initiateUkDomesticPaymentRequest) throws PaymentExecutionTechnicalException {
        return StarlingBankInitiatePaymentExecutionContextPreExecutionResult.builder()
                .basicAuthMeans(initiateUkDomesticPaymentRequest.getAuthenticationMeans())
                .providerStatePayload(UkDomesticPaymentProviderState.builder()
                        .externalPaymentId(UUID.randomUUID().toString())
                        .paymentRequest(initiateUkDomesticPaymentRequest.getRequestDTO())
                        .build())
                .state(initiateUkDomesticPaymentRequest.getState())
                .baseRedirectUrl(initiateUkDomesticPaymentRequest.getBaseClientRedirectUrl())
                .build();
    }
}
