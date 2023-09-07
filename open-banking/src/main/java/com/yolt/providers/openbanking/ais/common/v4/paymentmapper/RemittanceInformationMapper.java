package com.yolt.providers.openbanking.ais.common.v4.paymentmapper;

import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationRemittanceInformation;

public interface RemittanceInformationMapper {

    OBWriteDomestic2DataInitiationRemittanceInformation createRemittanceInformation(final String remittanceStructured, final String remittanceUnstructured);
}
