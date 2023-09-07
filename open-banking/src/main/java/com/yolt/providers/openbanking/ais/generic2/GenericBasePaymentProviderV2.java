package com.yolt.providers.openbanking.ais.generic2;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPeriodicPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.single.GenericInitiatePaymentPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.GenericPaymentStatusPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ServiceType;
import org.apache.commons.lang3.NotImplementedException;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class GenericBasePaymentProviderV2 implements UkDomesticPaymentProvider, PaymentSubmissionProvider {

    private final UkInitiatePaymentExecutionContextAdapter<OBWriteDomesticConsent4, OBWriteDomesticConsentResponse5, GenericInitiatePaymentPreExecutionResult> initiatePaymentExecutionContextAdapter;
    private final UkSubmitPaymentExecutionContextAdapter<OBWriteDomestic2, OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult> submitPaymentExecutionContextAdapter;
    private final UkStatusPaymentExecutionContextAdapter<Void, PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> statusPaymentExecutionContextAdapter;
    private final ProviderIdentification providerIdentification;
    private final Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeans;
    private final Supplier<Optional<KeyRequirements>> getSigningKeyRequirements;
    private final Supplier<Optional<KeyRequirements>> getTransportKeyRequirements;
    @NotNull
    private final ConsentValidityRules consentValidityRules;

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiateSinglePayment(InitiateUkDomesticPaymentRequest initiatePaymentRequest) {
        return initiatePaymentExecutionContextAdapter.initiateSinglePayment(initiatePaymentRequest);
    }

    @Override
    public PaymentStatusResponseDTO submitPayment(SubmitPaymentRequest submitPaymentRequest) {
        return submitPaymentExecutionContextAdapter.submitPayment(submitPaymentRequest);
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiatePeriodicPayment(InitiateUkDomesticPeriodicPaymentRequest initiatePaymentRequest) {
        throw new NotImplementedException();
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiateScheduledPayment(InitiateUkDomesticScheduledPaymentRequest initiatePaymentRequest) {
        throw new NotImplementedException();
    }

    @Override
    public PaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest) {
        return statusPaymentExecutionContextAdapter.getPaymentStatus(getStatusRequest);
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentification.getIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentification.getDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return providerIdentification.getVersion();
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.PIS;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedAuthenticationMeans.get();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return getSigningKeyRequirements.get();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return getTransportKeyRequirements.get();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return this.consentValidityRules != null ? this.consentValidityRules : ConsentValidityRules.EMPTY_RULES_SET;
    }
}
