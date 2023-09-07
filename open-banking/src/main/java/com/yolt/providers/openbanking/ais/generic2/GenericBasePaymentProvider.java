package com.yolt.providers.openbanking.ais.generic2;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPeriodicPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.UkDomesticPaymentValidator;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.pis.paymentservice.UkDomesticPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ServiceType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class GenericBasePaymentProvider implements UkDomesticPaymentProvider, PaymentSubmissionProvider {

    private static final String INVALID_PAYMENT_ERROR_MESSAGE = "Payment is not valid! This should've been caught earlier in the process.";

    private final UkDomesticPaymentService ukDomesticPaymentService;
    private final AuthenticationService authenticationService;
    private final HttpClientFactory httpClientFactory;
    private final TokenScope scope;
    private final ProviderIdentification providerIdentification;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeans;
    private final Supplier<Optional<KeyRequirements>> getSigningKeyRequirements;
    private final Supplier<Optional<KeyRequirements>> getTransportKeyRequirements;
    private final ConsentValidityRules consentValidityRules;

    public GenericBasePaymentProvider(UkDomesticPaymentService ukDomesticPaymentService, AuthenticationService authenticationService, HttpClientFactory httpClientFactory, TokenScope scope, ProviderIdentification providerIdentification, Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans, Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeans, Supplier<Optional<KeyRequirements>> getSigningKeyRequirements, Supplier<Optional<KeyRequirements>> getTransportKeyRequirements) {
        this.ukDomesticPaymentService = ukDomesticPaymentService;
        this.authenticationService = authenticationService;
        this.httpClientFactory = httpClientFactory;
        this.scope = scope;
        this.providerIdentification = providerIdentification;
        this.getAuthenticationMeans = getAuthenticationMeans;
        this.typedAuthenticationMeans = typedAuthenticationMeans;
        this.getSigningKeyRequirements = getSigningKeyRequirements;
        this.getTransportKeyRequirements = getTransportKeyRequirements;
        this.consentValidityRules = ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiateSinglePayment(InitiateUkDomesticPaymentRequest initiatePaymentRequest) throws CreationFailedException {
        if (!UkDomesticPaymentValidator.isValid(initiatePaymentRequest)) {
            throw new CreationFailedException(INVALID_PAYMENT_ERROR_MESSAGE);
        }
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(initiatePaymentRequest.getAuthenticationMeans());
        HttpClient httpClient = httpClientFactory.createHttpClient(initiatePaymentRequest.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());
        return ukDomesticPaymentService.createSinglePayment(httpClient, authenticationMeans, initiatePaymentRequest, scope);
    }

    @Override
    public PaymentStatusResponseDTO submitPayment(SubmitPaymentRequest submitPaymentRequest) throws ConfirmationFailedException {
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(submitPaymentRequest.getAuthenticationMeans());
        HttpClient httpClient = httpClientFactory.createHttpClient(submitPaymentRequest.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

        return ukDomesticPaymentService.confirmPayment(httpClient, authenticationMeans, submitPaymentRequest, scope);
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiateScheduledPayment(InitiateUkDomesticScheduledPaymentRequest initiatePaymentRequest) throws CreationFailedException {
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(initiatePaymentRequest.getAuthenticationMeans());
        HttpClient httpClient = httpClientFactory.createHttpClient(initiatePaymentRequest.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

        return ukDomesticPaymentService.createScheduledPayment(httpClient, authenticationMeans, initiatePaymentRequest, scope);
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiatePeriodicPayment(InitiateUkDomesticPeriodicPaymentRequest initiatePaymentRequest) throws CreationFailedException {
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(initiatePaymentRequest.getAuthenticationMeans());
        HttpClient httpClient = httpClientFactory.createHttpClient(initiatePaymentRequest.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

        return ukDomesticPaymentService.createPeriodicPayment(httpClient, authenticationMeans, initiatePaymentRequest, scope);
    }

    @Override
    public PaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest) {
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(getStatusRequest.getAuthenticationMeans());
        HttpClient httpClient = httpClientFactory.createHttpClient(getStatusRequest.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

        return ukDomesticPaymentService.getPaymentStatus(httpClient, authenticationMeans, getStatusRequest, scope);
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
    public final Optional<KeyRequirements> getSigningKeyRequirements() {
        return getSigningKeyRequirements.get();
    }

    @Override
    public final Optional<KeyRequirements> getTransportKeyRequirements() {
        return getTransportKeyRequirements.get();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return this.consentValidityRules != null ? this.consentValidityRules : ConsentValidityRules.EMPTY_RULES_SET;
    }
}
