package com.yolt.providers.openbanking.ais.generic2;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateScheduledPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled.GenericInitiateScheduledPaymentPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.single.GenericInitiatePaymentPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.GenericPaymentStatusPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.sepa.mapper.SepaPaymentMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class GenericBaseSepaPaymentProvider implements SepaPaymentProvider {

    private final UkInitiateSinglePaymentExecutionContextAdapter<OBWriteDomesticConsent4, OBWriteDomesticConsentResponse5, GenericInitiatePaymentPreExecutionResult> initiatePaymentExecutionContextAdapter;
    private final UkInitiateScheduledPaymentExecutionContextAdapter<OBWriteDomesticScheduledConsent4, OBWriteDomesticScheduledConsentResponse5, GenericInitiateScheduledPaymentPreExecutionResult> initiateScheduledPaymentExecutionContextAdapter;
    private final UkSubmitPaymentExecutionContextAdapter<OBWriteDomestic2, OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult> submitPaymentExecutionContextAdapter;
    private final UkSubmitPaymentExecutionContextAdapter<OBWriteDomesticScheduled2, OBWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult> submitScheduledPaymentExecutionContextAdapter;
    private final UkStatusPaymentExecutionContextAdapter<Void, PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> statusPaymentExecutionContextAdapter;
    private final UkStatusPaymentExecutionContextAdapter<Void, ScheduledPaymentStatusResponse, GenericPaymentStatusPreExecutionResult> statusScheduledPaymentExecutionContextAdapter;
    private final ProviderIdentification providerIdentification;
    private final Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeans;
    private final Supplier<Optional<KeyRequirements>> getSigningKeyRequirements;
    private final Supplier<Optional<KeyRequirements>> getTransportKeyRequirements;
    @NotNull
    private final ConsentValidityRules consentValidityRules;
    @NotNull
    private final UkProviderStateDeserializer ukProviderStateDeserializer;
    private final SepaPaymentMapper sepaPaymentMapper;

    @Override
    public LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest initiatePaymentRequest) {
        var ukRequest = sepaPaymentMapper.mapInitiateSinglePaymentRequest(initiatePaymentRequest);
        var ukResponse = initiatePaymentExecutionContextAdapter.initiateSinglePayment(ukRequest);
        return sepaPaymentMapper.mapInitiatePaymentResponse(ukResponse);
    }

    @Override
    public LoginUrlAndStateDTO initiateScheduledPayment(InitiatePaymentRequest initiateScheduledPaymentRequest) {
        var ukRequest = sepaPaymentMapper.mapInitiateScheduledPaymentRequest(initiateScheduledPaymentRequest);
        var ukResponse = initiateScheduledPaymentExecutionContextAdapter.initiateScheduledPayment(ukRequest);
        return sepaPaymentMapper.mapInitiatePaymentResponse(ukResponse);
    }

    @Override
    public SepaPaymentStatusResponseDTO submitPayment(SubmitPaymentRequest submitPaymentRequest) {
        var ukRequest = sepaPaymentMapper.mapSubmitPaymentRequest(submitPaymentRequest);
        var paymentType = ukProviderStateDeserializer.deserialize(submitPaymentRequest.getProviderState()).getPaymentType();
        var ukResponse = switch (paymentType) {
            case SINGLE -> submitPaymentExecutionContextAdapter.submitPayment(ukRequest);
            case SCHEDULED -> submitScheduledPaymentExecutionContextAdapter.submitPayment(ukRequest);
            case PERIODIC -> throw new NotImplementedException("Periodic payments are mot implemented yet");
        };

        return sepaPaymentMapper.mapSubmitPaymentResponse(ukResponse);
    }

    @Override
    public SepaPaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest) {
        var ukRequest = sepaPaymentMapper.mapStatusRequest(getStatusRequest);
        var paymentType = ukProviderStateDeserializer.deserialize(getStatusRequest.getProviderState()).getPaymentType();
        var ukResponse = switch (paymentType) {
            case SINGLE -> statusPaymentExecutionContextAdapter.getPaymentStatus(ukRequest);
            case SCHEDULED -> statusScheduledPaymentExecutionContextAdapter.getPaymentStatus(ukRequest);
            case PERIODIC -> throw new NotImplementedException("Periodic payments are mot implemented yet");
        };

        return sepaPaymentMapper.mapStatusResponse(ukResponse);
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
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedAuthenticationMeans.get();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return this.consentValidityRules != null ? this.consentValidityRules : ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return getSigningKeyRequirements.get();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return getTransportKeyRequirements.get();
    }
}
