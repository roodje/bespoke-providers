package com.yolt.providers.openbanking.ais.monzogroup.common.service.pis;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.util.regex.Pattern;

public class MonzoGroupPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {
    public static final String SORT_CODE_ACCOUNT_NUMBER_SCHEME = "UK.OBIE.SortCodeAccountNumber";
    private static final Pattern REFERENCE_REGEX = Pattern.compile("^[A-Za-z0-9\\/\\-?:().,'+ #=!\"%&*<>;{}@\\r\\n]{1,18}$");

    @Override
    public void validateRequest(OBWriteDomestic2DataInitiation dataInitiation) {
        String paymentSchemeName = dataInitiation.getCreditorAccount().getSchemeName();
        String reference = dataInitiation.getRemittanceInformation().getReference();

        if (!SORT_CODE_ACCOUNT_NUMBER_SCHEME.equals(paymentSchemeName)) {
            throw new IllegalArgumentException("Monzo PIS v3.1 support only SortCodeAccountNumber. Provided scheme: " + paymentSchemeName + " is not supported.");
        }

        CurrencyCode currencyName = CurrencyCode.valueOf(dataInitiation.getInstructedAmount().getCurrency());
        if (!CurrencyCode.GBP.equals(currencyName)) {
            throw new IllegalArgumentException("Monzo PIS v3.1 support only GBP currency. Provided currency: " + currencyName + " is not supported.");
        }
        if (reference == null || !REFERENCE_REGEX.matcher(reference).matches()) {
            throw new IllegalArgumentException("Remittance information contains not allowed characters. It should match " + REFERENCE_REGEX);
        }
    }
}
