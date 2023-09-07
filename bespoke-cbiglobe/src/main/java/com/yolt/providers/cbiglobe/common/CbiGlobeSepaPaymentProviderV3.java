package com.yolt.providers.cbiglobe.common;

import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.CbiGlobeSepaInitiatePreExecutionResult;
import com.yolt.providers.cbiglobe.common.pis.pec.submit.CbiGlobeSepaSubmitPreExecutionResult;
import com.yolt.providers.cbiglobe.common.util.HsmUtils;
import com.yolt.providers.cbiglobe.pis.dto.GetPaymentStatusRequestResponseType;
import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans.*;

@RequiredArgsConstructor
public class CbiGlobeSepaPaymentProviderV3 implements SepaPaymentProvider {

    private final SepaInitiatePaymentExecutionContextAdapter<com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest, PaymentInitiationRequestResponseType, CbiGlobeSepaInitiatePreExecutionResult> initiatePaymentExecutionContextAdapter;
    private final SepaSubmitPaymentExecutionContextAdapter<Void, GetPaymentStatusRequestResponseType, CbiGlobeSepaSubmitPreExecutionResult> submitPaymentExecutionContextAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<Void, GetPaymentStatusRequestResponseType, CbiGlobeSepaSubmitPreExecutionResult> statusPaymentExecutionContextAdapter;
    private final ProviderIdentification providerIdentification;
    private final ConsentValidityRules consentValidityRules;

    @Override
    public LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest initiatePaymentRequest) {
        return initiatePaymentExecutionContextAdapter.initiatePayment(initiatePaymentRequest);
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
        Map<String, TypedAuthenticationMeans> authMeans = new HashMap<>();
        authMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        authMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        authMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        authMeans.put(SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        authMeans.put(CLIENT_ID_STRING_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        authMeans.put(CLIENT_SECRET_STRING_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        return authMeans;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
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
