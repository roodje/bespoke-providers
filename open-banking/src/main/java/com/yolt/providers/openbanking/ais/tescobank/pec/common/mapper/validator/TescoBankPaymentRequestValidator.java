package com.yolt.providers.openbanking.ais.tescobank.pec.common.mapper.validator;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import org.springframework.util.StringUtils;

public class TescoBankPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {

    private static final int REFERENCE_MAX_LENGTH = 18;

    @Override
    public void validateRequest(OBWriteDomestic2DataInitiation dataInitiation) {
        String reference = dataInitiation.getRemittanceInformation().getReference();
        if (!StringUtils.isEmpty(reference) && reference.length() > REFERENCE_MAX_LENGTH) {
            throw new IllegalArgumentException("Reference in payment is too long (" + reference.length() + "), maximum allowed for Tesco is " + REFERENCE_MAX_LENGTH + " characters");
        }
    }
}
