package com.yolt.providers.unicredit.common.pis;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.unicredit.common.service.UniCreditAuthenticationMeansProducer;
import com.yolt.providers.unicredit.common.service.UniCreditSepaPaymentService;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.ing.lovebird.providerdomain.ServiceType;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class UniCreditSepaPaymentProvider implements SepaPaymentProvider {

    private final UniCreditAuthenticationMeansProducer authenticationMeansProducer;
    private final UniCreditSepaPaymentService sepaPaymentService;
    private final ProviderInfo providerInfo;
    private final ConsentValidityRules consentValidityRules;

    @SneakyThrows
    @Override
    public LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest initiatePaymentRequest) {
        return sepaPaymentService.initiatePayment(initiatePaymentRequest, providerInfo);
    }

    @SneakyThrows
    @Override
    public SepaPaymentStatusResponseDTO submitPayment(SubmitPaymentRequest submitPaymentRequest) {
        return sepaPaymentService.submitPayment(submitPaymentRequest, providerInfo);
    }

    @SneakyThrows
    @Override
    public SepaPaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest) {
        return sepaPaymentService.getStatus(getStatusRequest, providerInfo);
    }

    @Override
    public String getProviderIdentifier() {
        return providerInfo.getIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerInfo.getDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return providerInfo.getVersion();
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.PIS;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authenticationMeansProducer.getTransportKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return consentValidityRules != null ? consentValidityRules : ConsentValidityRules.EMPTY_RULES_SET;
    }
}
