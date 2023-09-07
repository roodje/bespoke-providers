package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.LoginUrlAndStateDTO;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;

public class SepaPaymentMapper {

    private final SepaInitiateSinglePaymentRequestMapper initiateSinglePaymentRequestMapper = new SepaInitiateSinglePaymentRequestMapper(new SepaDynamicFieldsMapper());
    private final SepaInitiatePaymentResponseMapper initiatePaymentResponseMapper = new SepaInitiatePaymentResponseMapper();
    private final SepaInitiateSheduledPaymentRequestMapper initiateScheduledPaymentRequestMapper = new SepaInitiateSheduledPaymentRequestMapper(new SepaDynamicFieldsMapper());
    private final SepaSubmitPaymentRequestMapper submitPaymentRequestMapper = new SepaSubmitPaymentRequestMapper();
    private final SepaSubmitPaymentResponseMapper submitPaymentResponseMapper = new SepaSubmitPaymentResponseMapper();
    private final SepaGetStatusRequestMapper statusRequestMapper = new SepaGetStatusRequestMapper();
    private final SepaGetStatusResponseMapper statusResponseMapper = new SepaGetStatusResponseMapper();

    public InitiateUkDomesticPaymentRequest mapInitiateSinglePaymentRequest(InitiatePaymentRequest sepaRequest) {
        return initiateSinglePaymentRequestMapper.map(sepaRequest);
    }

    public LoginUrlAndStateDTO mapInitiatePaymentResponse(InitiateUkDomesticPaymentResponseDTO ukResponse) {
        return initiatePaymentResponseMapper.map(ukResponse);
    }

    public InitiateUkDomesticScheduledPaymentRequest mapInitiateScheduledPaymentRequest(InitiatePaymentRequest sepaRequest) {
        return initiateScheduledPaymentRequestMapper.map(sepaRequest);
    }

    public SubmitPaymentRequest mapSubmitPaymentRequest(com.yolt.providers.common.pis.sepa.SubmitPaymentRequest sepaRequest) {
        return submitPaymentRequestMapper.map(sepaRequest);
    }

    public SepaPaymentStatusResponseDTO mapSubmitPaymentResponse(PaymentStatusResponseDTO ukResponse) {
        return submitPaymentResponseMapper.map(ukResponse);
    }

    public GetStatusRequest mapStatusRequest(com.yolt.providers.common.pis.sepa.GetStatusRequest sepaRequest) {
        return statusRequestMapper.map(sepaRequest);
    }

    public SepaPaymentStatusResponseDTO mapStatusResponse(PaymentStatusResponseDTO ukResponse) {
        return statusResponseMapper.map(ukResponse);
    }
}
