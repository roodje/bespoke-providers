package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePeriodicPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiatePeriodicPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateScheduledPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPeriodicPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.SepaInitiatePaymentResponse;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common.YoltBankSepaInitiatePaymentPreExecutionResult;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit.SepaPaymentStatusResponse;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit.YoltBankSepaSubmitPreExecutionResult;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticScheduledConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic.YoltBankUkInitiatePeriodicPaymentPreExecutionResult;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.YoltBankUkInitiateSinglePaymentPreExecutionResult;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled.YoltBankUkInitiateScheduledPaymentPreExecutionResult;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.YoltBankUkSubmitPreExecutionResult;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.model.PaymentSubmitResponse;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ServiceType;

import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

/**
 * This class also implements {@link SepaPaymentProvider} which is the 'newer' version of a payment interface. This should support B2B clients.
 * It is not an exact copy of the openbanking standard and should work on more banks. Furthermore, no AIS is needed to do PIS on the Sepa
 * payment interface/flow.
 * <p>
 * It also implements the {@link UkDomesticPaymentProvider} to allow our B2B clients to use and test all Uk Domestic payment functionality provided by Yolt.
 * <p>
 * This {@link YoltPaymentProvider} should be the B2B one-stop-shop for any sandbox testing with regards to payments.
 */
@RequiredArgsConstructor
public class YoltPaymentProvider implements SepaPaymentProvider, UkDomesticPaymentProvider, PaymentSubmissionProvider {

    private final SepaInitiateSinglePaymentExecutionContextAdapter<byte[], SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult> sepaInitiateSinglePaymentExecutionContextAdapter;
    private final SepaInitiatePeriodicPaymentExecutionContextAdapter<byte[], SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult> sepaInitiatePeriodicPaymentExecutionContextAdapter;
    private final SepaSubmitPaymentExecutionContextAdapter<Void, SepaPaymentStatusResponse, YoltBankSepaSubmitPreExecutionResult> sepaSubmitPaymentExecutionContextAdapter;
    private final SepaStatusPaymentExecutionContextAdapter<Void, SepaPaymentStatusResponse, YoltBankSepaSubmitPreExecutionResult> sepaStatusPaymentExecutionContextAdapter;

    private final UkInitiateSinglePaymentExecutionContextAdapter<OBWriteDomesticConsent1, InitiatePaymentConsentResponse, YoltBankUkInitiateSinglePaymentPreExecutionResult> ukInitiateSinglePaymentExecutionContextAdapter;
    private final UkInitiateScheduledPaymentExecutionContextAdapter<OBWriteDomesticScheduledConsent1, InitiatePaymentConsentResponse, YoltBankUkInitiateScheduledPaymentPreExecutionResult> ukInitiateScheduledPaymentExecutionContextAdapter;
    private final UkInitiatePeriodicPaymentExecutionContextAdapter<OBWriteDomesticStandingOrderConsent1, InitiatePaymentConsentResponse, YoltBankUkInitiatePeriodicPaymentPreExecutionResult> ukInitiatePeriodicPaymentExecutionContextAdapter;
    private final UkSubmitPaymentExecutionContextAdapter<com.yolt.providers.yoltprovider.pis.ukdomestic.ConfirmPaymentRequest, PaymentSubmitResponse, YoltBankUkSubmitPreExecutionResult> ukSubmitPaymentExecutionContextAdapter;
    private final UkStatusPaymentExecutionContextAdapter<Void, PaymentSubmitResponse, YoltBankUkSubmitPreExecutionResult> ukStatusPaymentExecutionContextAdapter;


    @Override
    public ServiceType getServiceType() {
        return ServiceType.PIS;
    }

    @Override
    public String getProviderIdentifier() {
        return "YOLT_PROVIDER";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Yolt Test Bank Provider";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_1;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return PaymentAuthenticationMeans.getTypedAuthenticationMeans();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return PaymentAuthenticationMeans.getSigningKeyRequirements();
    }

    // SEPA payment implementations

    @Override
    public LoginUrlAndStateDTO initiatePayment(final InitiatePaymentRequest initiatePaymentRequest) {
        return sepaInitiateSinglePaymentExecutionContextAdapter.initiateSinglePayment(initiatePaymentRequest);
    }

    @Override
    public SepaPaymentStatusResponseDTO submitPayment(final SubmitPaymentRequest submitPaymentRequest) {
        return sepaSubmitPaymentExecutionContextAdapter.submitPayment(submitPaymentRequest);
    }

    @Override
    public SepaPaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest) {
        return sepaStatusPaymentExecutionContextAdapter.getPaymentStatus(getStatusRequest);
    }

    @Override
    public LoginUrlAndStateDTO initiateScheduledPayment(final InitiatePaymentRequest initiateScheduledPaymentRequest) {
        //there is no separate flow for initiating scheduled payment we use single payment flow
        return initiatePayment(initiateScheduledPaymentRequest);
    }

    @Override
    public LoginUrlAndStateDTO initiatePeriodicPayment(final InitiatePaymentRequest initiatePeriodicPaymentRequest) {
        return sepaInitiatePeriodicPaymentExecutionContextAdapter.initiatePeriodicPayment(initiatePeriodicPaymentRequest);
    }

    // Uk Domestic implementations

    @Override
    public PaymentStatusResponseDTO submitPayment(com.yolt.providers.common.pis.common.SubmitPaymentRequest request) {
        return ukSubmitPaymentExecutionContextAdapter.submitPayment(request);
    }

    @Override
    public PaymentStatusResponseDTO getStatus(com.yolt.providers.common.pis.common.GetStatusRequest getStatusRequest) {
        return ukStatusPaymentExecutionContextAdapter.getPaymentStatus(getStatusRequest);
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiateSinglePayment(InitiateUkDomesticPaymentRequest initiateRequest) {
        return ukInitiateSinglePaymentExecutionContextAdapter.initiateSinglePayment(initiateRequest);
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiateScheduledPayment(final InitiateUkDomesticScheduledPaymentRequest initiatePaymentRequest) {
        return ukInitiateScheduledPaymentExecutionContextAdapter.initiateScheduledPayment(initiatePaymentRequest);
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO initiatePeriodicPayment(final InitiateUkDomesticPeriodicPaymentRequest initiatePaymentRequest) {
        return ukInitiatePeriodicPaymentExecutionContextAdapter.initiatePeriodicPayment(initiatePaymentRequest);
    }
}
