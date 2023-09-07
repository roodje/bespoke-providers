package com.yolt.providers.rabobank.pis;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.rabobank.HsmUtils;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;
import com.yolt.providers.rabobank.dto.external.SepaCreditTransfer;
import com.yolt.providers.rabobank.dto.external.StatusResponse;
import com.yolt.providers.rabobank.pis.pec.initiate.RabobankSepaInitiatePreExecutionResult;
import com.yolt.providers.rabobank.pis.pec.submit.RabobankSepaSubmitPaymentPreExecutionResult;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.rabobank.RabobankAuthenticationMeans.*;

@RequiredArgsConstructor
public class RabobankPaymentProvider implements SepaPaymentProvider {

    private final SepaInitiatePaymentExecutionContextAdapter<SepaCreditTransfer, InitiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult> sepaInitiatedPaymentExecutionContextAdapter;
    private final SepaSubmitPaymentExecutionContextAdapter<Void, StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult> sepaSubmitPaymentExecutionContextAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<Void, StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult> sepaStatusPaymentExecutionContextAdapter;
    private final String providersIdentifier;
    private final String providerDisplayName;
    private final ProviderVersion version;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return getTypedAuthenticationMeansForAISAndPIS();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(CLIENT_SIGNING_KEY_ID, CLIENT_SIGNING_CERTIFICATE);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID, CLIENT_TRANSPORT_CERTIFICATE);
    }

    @Override
    public LoginUrlAndStateDTO initiatePayment(final InitiatePaymentRequest initiatePaymentRequest) {
        return sepaInitiatedPaymentExecutionContextAdapter.initiatePayment(initiatePaymentRequest);
    }

    @Override
    public SepaPaymentStatusResponseDTO submitPayment(final SubmitPaymentRequest submitPaymentRequest) {
        // At rabobank the payment is submitted by the user through the consent button. We can only retrieve the status right now.
        return sepaSubmitPaymentExecutionContextAdapter.submitPayment(submitPaymentRequest);
    }

    @Override
    public SepaPaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest) {
        return sepaStatusPaymentExecutionContextAdapter.getPaymentStatus(getStatusRequest);
    }

    @Override
    public String getProviderIdentifier() {
        return providersIdentifier;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerDisplayName;
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
