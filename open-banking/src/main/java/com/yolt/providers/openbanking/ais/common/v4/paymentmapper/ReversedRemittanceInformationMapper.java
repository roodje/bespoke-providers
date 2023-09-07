package com.yolt.providers.openbanking.ais.common.v4.paymentmapper;

import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationRemittanceInformation;
import org.springframework.util.StringUtils;

public class ReversedRemittanceInformationMapper implements RemittanceInformationMapper {

    @Override
    public OBWriteDomestic2DataInitiationRemittanceInformation createRemittanceInformation(String remittanceStructured, String remittanceUnstructured) {
        if (StringUtils.isEmpty(remittanceStructured) && StringUtils.isEmpty(remittanceUnstructured)) {
            return null;
        }
        return new OBWriteDomestic2DataInitiationRemittanceInformation()
                .reference(remittanceUnstructured)
                .unstructured(remittanceStructured);
    }
}
