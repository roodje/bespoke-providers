package com.yolt.providers.openbanking.ais.monzogroup.common.service.pis.ukdomesticpaymentservice;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.UkPaymentMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MonzoUkPaymentMapperDecorator implements UkPaymentMapper {

    private static final String LOCAL_INSTRUMENT = "UK.OBIE.FPS";

    private final UkPaymentMapper wrappe;

    @Override
    public OBWriteDomesticConsent4 mapToSetupRequest(InitiateUkDomesticPaymentRequest request) {
        OBWriteDomesticConsent4 consent = wrappe.mapToSetupRequest(request);
        consent.getData().getInitiation().setLocalInstrument(LOCAL_INSTRUMENT);
        return consent;
    }

    @Override
    public OBWriteDomestic2 mapToSubmitRequest(String consentId, OBWriteDomestic2DataInitiation paymentIntent) {
        return wrappe.mapToSubmitRequest(consentId, paymentIntent);
    }
}
