package com.yolt.providers.bunq.common;

import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentRequest;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentResponse;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentStatusResponse;
import com.yolt.providers.bunq.common.pis.pec.initiate.DefaultInitiatePaymentPreExecutionResult;
import com.yolt.providers.bunq.common.pis.pec.submitandstatus.DefaultSubmitAndStatusPaymentPreExecutionResult;
import com.yolt.providers.bunq.common.util.HsmUtils;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2.SIGNING_CERTIFICATE;
import static com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2.SIGNING_PRIVATE_KEY_ID;

@RequiredArgsConstructor
public class BunqPaymentProvider implements SepaPaymentProvider {

    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion version;
    private final SepaInitiateSinglePaymentExecutionContextAdapter<PaymentServiceProviderDraftPaymentRequest, PaymentServiceProviderDraftPaymentResponse, DefaultInitiatePaymentPreExecutionResult> initiateSinglePaymentExecutionContextAdapter;
    private final SepaSubmitPaymentExecutionContextAdapter<Void, PaymentServiceProviderDraftPaymentStatusResponse, DefaultSubmitAndStatusPaymentPreExecutionResult> submitPaymentExecutionContextAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<Void, PaymentServiceProviderDraftPaymentStatusResponse, DefaultSubmitAndStatusPaymentPreExecutionResult> statusPaymentExecutionContextAdapter;

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
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return BunqAuthenticationMeansV2.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID, SIGNING_CERTIFICATE);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
