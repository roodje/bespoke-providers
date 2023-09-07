package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.sepa.LoginUrlAndStateDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;

public class SepaInitiatePaymentResponseMapper {

    public LoginUrlAndStateDTO map(InitiateUkDomesticPaymentResponseDTO ukResponse) {
        return new LoginUrlAndStateDTO(
                ukResponse.getLoginUrl(),
                ukResponse.getProviderState(),
                ukResponse.getPaymentExecutionContextMetadata()
        );
    }
}
