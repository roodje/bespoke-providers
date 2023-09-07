package com.yolt.providers.abnamrogroup.common.pis;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamro.pis.SepaPayment;
import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.abnamrogroup.common.pis.pec.initiate.AbnAmroInitiatePaymentPreExecutionResult;
import com.yolt.providers.abnamrogroup.common.pis.pec.status.AbnAmroPaymentStatusPreExecutionResult;
import com.yolt.providers.abnamrogroup.common.pis.pec.submit.AbnAmroSubmitPaymentPreExecutionResult;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans.*;
import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@RequiredArgsConstructor
public class AbnAmroPaymentProvider implements SepaPaymentProvider {

    private final String providerIdentifier;
    private final String providerDisplayName;
    private final SepaInitiatePaymentExecutionContextAdapter<SepaPayment, InitiatePaymentResponseDTO, AbnAmroInitiatePaymentPreExecutionResult> initiatePaymentExecutionContextAdapter;
    private final SepaSubmitPaymentExecutionContextAdapter<Void, TransactionStatusResponse, AbnAmroSubmitPaymentPreExecutionResult> submitPaymentExecutionContextAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<Void, TransactionStatusResponse, AbnAmroPaymentStatusPreExecutionResult> paymentStatusExecutionContextAdapter;

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
        return paymentStatusExecutionContextAdapter.getPaymentStatus(getStatusRequest);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return getStringTypedAuthenticationMeansMapForAisAndPis();
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerDisplayName;
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_1;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return AbnAmroAuthenticationMeans.getTransportKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return new ConsentValidityRules(new HashSet<>(Arrays.asList(
                "NL** ABNA",
                "Inloggen"
        )));
    }
}
