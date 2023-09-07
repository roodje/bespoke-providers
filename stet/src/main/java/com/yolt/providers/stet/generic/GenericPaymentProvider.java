package com.yolt.providers.stet.generic;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@RequiredArgsConstructor
public abstract class GenericPaymentProvider implements SepaPaymentProvider {

    private final AuthenticationMeansSupplier authMeansSupplier;
    private final HttpClientFactory httpClientFactory;
    private final PaymentService paymentService;
    private final DefaultProperties properties;
    private final ConsentValidityRules consentValidityRules;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return authMeansSupplier.getTypedAuthMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return authMeansSupplier.getTransportKeyRequirements();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return authMeansSupplier.getSigningKeyRequirements();
    }

    @Override
    public LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest request) {
        DefaultAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        HttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authMeans);
        return paymentService.initiatePayment(httpClient, request, authMeans);
    }

    @Override
    public SepaPaymentStatusResponseDTO submitPayment(SubmitPaymentRequest request) {
        DefaultAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        HttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authMeans);
        return paymentService.confirmPayment(httpClient, request, authMeans);
    }

    @Override
    public SepaPaymentStatusResponseDTO getStatus(GetStatusRequest request) {
        DefaultAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        HttpClient httpClient = createHttpClient(request.getRestTemplateManager(), authMeans);
        return paymentService.getPaymentStatus(httpClient, request, authMeans);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return consentValidityRules != null ? consentValidityRules : ConsentValidityRules.EMPTY_RULES_SET;
    }

    protected DefaultAuthenticationMeans createAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans) {
        return authMeansSupplier.getAuthMeans(basicAuthMeans, getProviderIdentifier());
    }

    protected HttpClient createHttpClient(RestTemplateManager restTemplateManager, DefaultAuthenticationMeans authMeans) {
        String baseUrl = properties.getRegions().get(0).getBaseUrl();
        return httpClientFactory.createHttpClient(restTemplateManager, authMeans, baseUrl, getProviderIdentifierDisplayName());
    }
}
