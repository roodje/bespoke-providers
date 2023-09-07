package com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LocalInstrumentUkPaymentMapperDecorator implements UkPaymentMapper {
    private final UkPaymentMapper wrappee;
    private final String localInstrument;

    @Override
    public OBWriteDomesticConsent4 mapToSetupRequest(InitiateUkDomesticPaymentRequest request) {
        OBWriteDomesticConsent4 setupRequest = wrappee.mapToSetupRequest(request);
        setupRequest.getData().getInitiation().setLocalInstrument(localInstrument);
        return setupRequest;
    }

    @Override
    public OBWriteDomestic2 mapToSubmitRequest(String consentId, OBWriteDomestic2DataInitiation paymentIntent) {
        return wrappee.mapToSubmitRequest(consentId, paymentIntent);
    }
}
