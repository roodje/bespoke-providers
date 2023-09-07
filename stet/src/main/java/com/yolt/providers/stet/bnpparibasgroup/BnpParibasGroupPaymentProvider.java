package com.yolt.providers.stet.bnpparibasgroup;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;


public class BnpParibasGroupPaymentProvider extends GenericPaymentProviderV3 {

    public BnpParibasGroupPaymentProvider(ProviderIdentification identification,
                                          SepaInitiateSinglePaymentExecutionContextAdapter<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> initiatePaymentExecutionContextAdapter,
                                          SepaSubmitPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, ? extends StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> submitPaymentExecutionContextAdapter,
                                          SepaStatusPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> statusPaymentExecutionContextAdapter,
                                          AuthenticationMeansSupplier authMeansSupplier,
                                          ConsentValidityRules consentValidityRules) {
        super(identification,
                initiatePaymentExecutionContextAdapter,
                submitPaymentExecutionContextAdapter,
                statusPaymentExecutionContextAdapter,
                authMeansSupplier,
                consentValidityRules);
    }

    @Override
    public SepaPaymentStatusResponseDTO submitPayment(SubmitPaymentRequest request) {
        GetStatusRequest getStatusRequest = new GetStatusRequest(
                request.getProviderState(),
                null,
                request.getAuthenticationMeans(),
                request.getSigner(),
                request.getRestTemplateManager(),
                request.getPsuIpAddress(),
                request.getAuthenticationMeansReference()
        );
        return super.getStatus(getStatusRequest);
    }
}
