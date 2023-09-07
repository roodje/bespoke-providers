package com.yolt.providers.starlingbank.common;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.starlingbank.common.auth.HsmEIdasUtils;
import com.yolt.providers.starlingbank.common.model.PaymentRequest;
import com.yolt.providers.starlingbank.common.model.PaymentStatusResponse;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankInitiatePaymentExecutionContextPreExecutionResult;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ServiceType;
import nl.ing.lovebird.providershared.api.ApiConfirmPaymentResponseDTO;
import nl.ing.lovebird.providershared.api.ApiCreatePaymentResponseDTO;
import nl.ing.lovebird.providershared.api.ApiDataProviderAuthorizeInfoDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans.*;

@RequiredArgsConstructor
public class StarlingBankPaymentProviderV2 implements UkDomesticPaymentProvider, PaymentSubmissionProvider {

    private final UkInitiatePaymentExecutionContextAdapter<String, String, StarlingBankInitiatePaymentExecutionContextPreExecutionResult> initiatePaymentExecutionContext;
    private final UkSubmitPaymentExecutionContextAdapter<PaymentRequest, PaymentSubmissionResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> submitPaymentExecutionContext;
    private final UkStatusPaymentExecutionContextAdapter<Object, PaymentStatusResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> statusPaymentExecutionContextAdapter;
    @Getter
    private final String providerIdentifier;
    @Getter
    private final String providerIdentifierDisplayName;
    @Getter
    private final ProviderVersion version;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(API_KEY_NAME_2, API_KEY_STRING);
        typedAuthMeans.put(API_SECRET_NAME_2, API_SECRET_STRING);
        typedAuthMeans.put(SIGNING_KEY_HEADER_ID_NAME_2, KEY_ID_HEADER_STRING);
        typedAuthMeans.put(SIGNING_PRIVATE_KEY_ID_NAME_2, KEY_ID);
        typedAuthMeans.put(TRANSPORT_KEY_ID_NAME_2, KEY_ID);
        typedAuthMeans.put(TRANSPORT_CERTIFICATE_NAME_2, CERTIFICATE_PEM);
        return typedAuthMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmEIdasUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME_2, TRANSPORT_CERTIFICATE_NAME_2);
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmEIdasUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME_2);
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.PIS;
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiateSinglePayment(InitiateUkDomesticPaymentRequest initiatePaymentRequest) throws CreationFailedException {
        return initiatePaymentExecutionContext.initiateSinglePayment(initiatePaymentRequest);
    }

    @Override
    public PaymentStatusResponseDTO submitPayment(SubmitPaymentRequest submitPaymentRequest) {
        return submitPaymentExecutionContext.submitPayment(submitPaymentRequest);
    }

    @Override
    public PaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest) {
        return statusPaymentExecutionContextAdapter.getPaymentStatus(getStatusRequest);
    }
    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
