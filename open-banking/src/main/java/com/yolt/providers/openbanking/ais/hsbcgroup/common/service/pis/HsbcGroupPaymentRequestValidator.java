package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.pis;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;

import java.util.regex.Pattern;

public class HsbcGroupPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {

    private static final Pattern REFERENCE_REGEX = Pattern.compile("^[A-Za-z0-9\\/\\s\\.\\+\\:\\(,\\&\\)-?]{1,18}$");

    @Override
    public void validateRequest(OBWriteDomestic2DataInitiation dataInitiation) {
        String reference = dataInitiation.getRemittanceInformation().getReference();

        if (reference == null || !REFERENCE_REGEX.matcher(reference).matches()) {
            throw new IllegalArgumentException("Remittance information contains not allowed characters. It should match " + REFERENCE_REGEX);
        }
    }
}
