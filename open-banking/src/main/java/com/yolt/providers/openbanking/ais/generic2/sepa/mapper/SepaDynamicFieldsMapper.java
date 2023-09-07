package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.sepa.DynamicFields;
import com.yolt.providers.common.providerdetail.dto.DynamicFieldNames;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class SepaDynamicFieldsMapper {

    public Map<String, String> map(DynamicFields dynamicFields) {
        var result = new HashMap<String, String>();

        if (dynamicFields == null) {
            return result;
        }

        if (StringUtils.hasText(dynamicFields.getRemittanceInformationStructured())) {
            result.put(DynamicFieldNames.REMITTANCE_INFORMATION_STRUCTURED.getValue(), dynamicFields.getRemittanceInformationStructured());
        }

        if (StringUtils.hasText(dynamicFields.getCreditorAgentBic())) {
            result.put(DynamicFieldNames.CREDITOR_AGENT_BIC.getValue(), dynamicFields.getCreditorAgentBic());
        }

        if (StringUtils.hasText(dynamicFields.getCreditorAgentName())) {
            result.put(DynamicFieldNames.CREDITOR_AGENT_NAME.getValue(), dynamicFields.getCreditorAgentName());
        }

        if (StringUtils.hasText(dynamicFields.getDebtorName())) {
            result.put(DynamicFieldNames.DEBTOR_NAME.getValue(), dynamicFields.getDebtorName());
        }

        if (StringUtils.hasText(dynamicFields.getCreditorPostalAddressLine())) {
            result.put(DynamicFieldNames.CREDITOR_POSTAL_ADDRESS_LINE.getValue(), dynamicFields.getCreditorPostalAddressLine());
        }

        if (StringUtils.hasText(dynamicFields.getCreditorPostalCountry())) {
            result.put(DynamicFieldNames.CREDITOR_POSTAL_COUNTRY.getValue(), dynamicFields.getCreditorPostalCountry());
        }

        return result;
    }
}
