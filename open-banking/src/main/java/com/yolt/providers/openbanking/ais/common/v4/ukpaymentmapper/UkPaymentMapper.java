package com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;

public interface UkPaymentMapper {

    OBWriteDomesticConsent4 mapToSetupRequest(final InitiateUkDomesticPaymentRequest request);

    OBWriteDomestic2 mapToSubmitRequest(final String consentId, OBWriteDomestic2DataInitiation paymentIntent);
}
