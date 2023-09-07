package com.yolt.providers.volksbank.common.pis;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.pis.pec.initiate.VolksbankSepaInitiatePreExecutionResult;
import com.yolt.providers.volksbank.common.pis.pec.submit.VolksbankSepaSubmitPreExecutionResult;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;
import com.yolt.providers.volksbank.dto.v1_1.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class VolksbankSepaPaymentProviderV3 implements SepaPaymentProvider {

    private final SepaInitiateSinglePaymentExecutionContextAdapter<com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentRequest, InitiatePaymentResponse, VolksbankSepaInitiatePreExecutionResult> initiatePaymentExecutionContextAdapter;
    private final SepaSubmitPaymentExecutionContextAdapter<Void, PaymentStatus, VolksbankSepaSubmitPreExecutionResult> submitPaymentExecutionContextAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<Void, PaymentStatus, VolksbankSepaSubmitPreExecutionResult> statusPaymentExecutionContextAdapter;
    private final ProviderIdentification providerIdentification;
    private final ConsentValidityRules consentValidityRules;

    @Override
    public LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest initiatePaymentRequest) {
        return initiatePaymentExecutionContextAdapter.initiateSinglePayment(initiatePaymentRequest);
    }

    @Override
    public SepaPaymentStatusResponseDTO submitPayment(SubmitPaymentRequest submitPaymentRequest) {
        return submitPaymentExecutionContextAdapter.submitPayment(submitPaymentRequest);
    }

    @Override
    public SepaPaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest) {
        return statusPaymentExecutionContextAdapter.getPaymentStatus(getStatusRequest);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return VolksbankAuthenticationMeans.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return VolksbankAuthenticationMeans.getTransportKeyRequirements();
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentification.getProviderIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentification.getProviderDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return providerIdentification.getProviderVersion();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return consentValidityRules;
    }
}
