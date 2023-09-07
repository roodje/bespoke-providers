package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;

public class SepaSubmitPaymentResponseMapper {

    public SepaPaymentStatusResponseDTO map(PaymentStatusResponseDTO ukResponse) {
        return new SepaPaymentStatusResponseDTO(
                ukResponse.getProviderState(),
                ukResponse.getPaymentId(),
                ukResponse.getPaymentExecutionContextMetadata()
        );
    }
}
