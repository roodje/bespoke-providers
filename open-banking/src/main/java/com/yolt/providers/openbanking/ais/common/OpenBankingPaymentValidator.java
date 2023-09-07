package com.yolt.providers.openbanking.ais.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.providershared.ProviderPayment;
import org.apache.commons.lang3.StringUtils;

/**
 * Performs additional validation beside the existing validation constraints defined on {@link ProviderPayment}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenBankingPaymentValidator {

    public static boolean isValid(ProviderPayment payment) {

        if (payment == null) {
            return false;
        }

        try {
            payment.getCreditorAccount().validate();
        } catch (IllegalArgumentException e) {
            return false;
        }
        return !StringUtils.isBlank(payment.getCreditorAccount().getHolderName());
    }
}
