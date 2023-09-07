package com.yolt.providers.knabgroup.common;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentRequestBody;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentResponse;
import com.yolt.providers.knabgroup.common.payment.dto.external.StatusPaymentResponse;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans.*;
import static com.yolt.providers.knabgroup.common.configuration.KnabGroupKeyRequirements.KNAB_GROUP_KEY_REQUIREMENTS;

@RequiredArgsConstructor
public class KnabGroupPaymentProvider implements SepaPaymentProvider {

    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion version;
    private final ConsentValidityRules consentValidityRules;

    private final SepaInitiateSinglePaymentExecutionContextAdapter<InitiatePaymentRequestBody, InitiatePaymentResponse, InitiatePaymentPreExecutionResult> initiateSinglePaymentExecutionContextAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<Void, StatusPaymentResponse, StatusPaymentPreExecutionResult> statusPaymentExecutionContextAdapter;
    private final SepaSubmitPaymentExecutionContextAdapter<Void, StatusPaymentResponse, StatusPaymentPreExecutionResult> submitPaymentExecutionContextAdapter;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(new KeyRequirements(KNAB_GROUP_KEY_REQUIREMENTS, TRANSPORT_KEY_ID, TRANSPORT_CERTIFICATE_NAME));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(new KeyRequirements(KNAB_GROUP_KEY_REQUIREMENTS, SIGNING_KEY_ID, SIGNING_CERTIFICATE_NAME));
    }

    @Override
    public LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest initiatePaymentRequest) {
        return initiateSinglePaymentExecutionContextAdapter.initiateSinglePayment(initiatePaymentRequest);
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
        return consentValidityRules;
    }
}
