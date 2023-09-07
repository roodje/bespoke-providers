package com.yolt.providers.starlingbank.common.model;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

@Data
@Builder
public class UkDomesticPaymentProviderState {

    String externalPaymentId;
    InitiateUkDomesticPaymentRequestDTO paymentRequest;
    String refreshToken;
    String accessToken;
    Date accessTokenExpiresIn;

    public boolean isPaymentNotSubmittedYet() {
        return Objects.isNull(this.accessToken);
    }
}
