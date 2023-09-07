package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.pec.common.mapper.validator;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import org.apache.commons.lang3.StringUtils;

public class LloydsBankingGroupPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {

    private static final int REFERENCE_MAX_LENGTH = 35;

    @Override
    public void validateRequest(OBWriteDomestic2DataInitiation dataInitiation) {
        String reference = dataInitiation.getRemittanceInformation().getReference();
        if (StringUtils.isNotEmpty(reference) && reference.length() > REFERENCE_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("Remittance Information Reference is too long %d. Maximum length for Lloyds Banking Group is %d", reference.length(), REFERENCE_MAX_LENGTH));
        }
    }
}
