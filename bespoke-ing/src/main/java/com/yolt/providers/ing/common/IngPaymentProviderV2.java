package com.yolt.providers.ing.common;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.ing.common.config.HsmUtils;
import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;
import com.yolt.providers.ing.common.dto.PaymentStatusResponse;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import com.yolt.providers.ing.common.pec.initiate.DefaultInitiatePaymentPreExecutionResult;
import com.yolt.providers.ing.common.pec.submit.DefaultSubmitPaymentPreExecutionResult;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.ing.common.auth.IngAuthenticationMeans.*;

@RequiredArgsConstructor
public class IngPaymentProviderV2 implements SepaPaymentProvider {

    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion version;

    private final SepaInitiatePaymentExecutionContextAdapter<SepaCreditTransfer, InitiatePaymentResponse, DefaultInitiatePaymentPreExecutionResult> initiatePaymentExecutionContextAdapter;
    private final SepaSubmitPaymentExecutionContextAdapter<Void, PaymentStatusResponse, DefaultSubmitPaymentPreExecutionResult> submitPaymentExecutionContextAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<Void, PaymentStatusResponse, DefaultSubmitPaymentPreExecutionResult> statusPaymentExecutionContextAdapter;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);

        return typedAuthenticationMeansMap;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_KEY_ID, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID, TRANSPORT_CERTIFICATE_NAME);
    }

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
    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentifierDisplayName;
    }

    @Override
    public ProviderVersion getVersion() {
        return version;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
