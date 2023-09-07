package com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper;

import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationRemittanceInformation;
import org.springframework.util.StringUtils;

public class ReversedUkRemittanceInformationMapper implements UkRemittanceInformationMapper {
    @Override
    public OBWriteDomestic2DataInitiationRemittanceInformation createRemittanceInformation(String remittanceReference, String remittanceInformationUnstructured) {
        if (StringUtils.isEmpty(remittanceReference) && StringUtils.isEmpty(remittanceInformationUnstructured)) {
            return null;
        }
        return new OBWriteDomestic2DataInitiationRemittanceInformation()
                .reference(remittanceInformationUnstructured)
                .unstructured(remittanceReference);
    }
}
