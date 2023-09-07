package com.yolt.providers.fineco;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.fineco.dto.PaymentRequest;
import com.yolt.providers.fineco.dto.PaymentResponse;
import com.yolt.providers.fineco.pis.initiate.FinecoInitiatePaymentPreExecutionResult;
import com.yolt.providers.fineco.pis.status.FinecoStatusPaymentPreExecutionResult;
import com.yolt.providers.fineco.util.HsmUtils;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.fineco.FinecoDetailsProvider.PROVIDER_DISPLAY_NAME;
import static com.yolt.providers.fineco.FinecoDetailsProvider.PROVIDER_KEY;
import static com.yolt.providers.fineco.auth.FinecoAuthenticationMeans.*;

@RequiredArgsConstructor
public class FinecoPaymentProvider implements SepaPaymentProvider {

    private final SepaInitiateSinglePaymentExecutionContextAdapter<PaymentRequest, PaymentResponse, FinecoInitiatePaymentPreExecutionResult> initiatePecAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<Void, PaymentResponse, FinecoStatusPaymentPreExecutionResult> statusPecAdapter;
    private final ProviderVersion version;

    @Override
    public LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest request) {
        return initiatePecAdapter.initiateSinglePayment(request);
    }

    /**
     * Fineco does not support submit flow.
     * Payment will be processed by the bank after user authentication, hence we are just forwarding paymentId
     *
     * @param request
     * @return
     */
    @Override
    public SepaPaymentStatusResponseDTO submitPayment(SubmitPaymentRequest request) {
        return new SepaPaymentStatusResponseDTO(request.getProviderState());
    }

    @Override
    public SepaPaymentStatusResponseDTO getStatus(GetStatusRequest request) {
        return statusPecAdapter.getPaymentStatus(request);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        final Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);

        return typedAuthenticationMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public String getProviderIdentifier() {
        return PROVIDER_KEY;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return PROVIDER_DISPLAY_NAME;
    }

    @Override
    public ProviderVersion getVersion() {
        return version;
    }
}
