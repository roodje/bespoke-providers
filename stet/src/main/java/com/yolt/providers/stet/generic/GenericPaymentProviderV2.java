package com.yolt.providers.stet.generic;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Deprecated
public class GenericPaymentProviderV2 implements SepaPaymentProvider {

    private final ProviderIdentification identification;
    private final SepaInitiatePaymentExecutionContextAdapter<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> initiatePaymentExecutionContextAdapter;
    private final SepaSubmitPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, ? extends StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> submitPaymentExecutionContextAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> statusPaymentExecutionContextAdapter;
    private final AuthenticationMeansSupplier authMeansSupplier;
    private final ConsentValidityRules consentValidityRules;

    @Override
    public String getProviderIdentifier() {
        return identification.getIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return identification.getDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return identification.getVersion();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authMeansSupplier.getTypedAuthMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authMeansSupplier.getTransportKeyRequirements();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return authMeansSupplier.getSigningKeyRequirements();
    }

    @Override
    public LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest request) {
        return initiatePaymentExecutionContextAdapter.initiatePayment(request);
    }

    @Override
    public SepaPaymentStatusResponseDTO submitPayment(SubmitPaymentRequest request) {
        return submitPaymentExecutionContextAdapter.submitPayment(request);
    }

    @Override
    public SepaPaymentStatusResponseDTO getStatus(GetStatusRequest request) {
        return statusPaymentExecutionContextAdapter.getPaymentStatus(request);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return consentValidityRules;
    }
}
