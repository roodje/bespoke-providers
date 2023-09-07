package com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper;

import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationRemittanceInformation;

public interface UkRemittanceInformationMapper {

    OBWriteDomestic2DataInitiationRemittanceInformation createRemittanceInformation(String remittanceReference, String remittanceInformationUnstructured);
}
