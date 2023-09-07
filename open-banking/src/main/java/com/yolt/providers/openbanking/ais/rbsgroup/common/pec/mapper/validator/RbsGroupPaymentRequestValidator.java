package com.yolt.providers.openbanking.ais.rbsgroup.common.pec.mapper.validator;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;

import java.util.regex.Pattern;

public class RbsGroupPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {

    private static final int END_TO_END_IDENTIFICATION_MAX_LENGTH = 30;
    private static final Pattern REFERENCE_REGEX = Pattern.compile("^[A-Za-z0-9\\/\\s\\.\\+\\:\\(,\\&\\')-?]{1,140}$");

    @Override
    public void validateRequest(OBWriteDomestic2DataInitiation dataInitiation) {
        verifyEndToEndIdentificationDoesNotExceedLimit(dataInitiation.getEndToEndIdentification());
        verifyRemittanceInformationReferenceDoesNotExceedLimit(dataInitiation.getRemittanceInformation().getReference());
    }

    private void verifyEndToEndIdentificationDoesNotExceedLimit(String endToEndIdentification) {
        if (endToEndIdentification.length() > END_TO_END_IDENTIFICATION_MAX_LENGTH) {
            throw new IllegalArgumentException("EndToEndIdentification in payment is too long (" + endToEndIdentification.length() + "), maximum allowed for RBS is " + END_TO_END_IDENTIFICATION_MAX_LENGTH + " characters");
        }
    }

    private void verifyRemittanceInformationReferenceDoesNotExceedLimit(String reference) {

        if (reference != null && !REFERENCE_REGEX.matcher(reference).matches()) {
            throw new IllegalArgumentException("Remittance information contains not allowed characters. It should match " + REFERENCE_REGEX);
        }
    }
}
