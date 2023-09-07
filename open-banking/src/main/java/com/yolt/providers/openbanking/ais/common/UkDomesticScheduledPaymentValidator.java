package com.yolt.providers.openbanking.ais.common;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Performs additional validation beside the existing validation constraints defined on {@link InitiateUkDomesticScheduledPaymentRequest}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UkDomesticScheduledPaymentValidator {

    public static boolean isValid(InitiateUkDomesticScheduledPaymentRequest payment) {
        if (payment == null) {
            return false;
        }
        UkAccountDTO creditorAccount = payment.getRequestDTO().getCreditorAccount();
        return creditorAccount.getAccountIdentifierScheme() != null &&
                creditorAccount.getAccountIdentifier() != null &&
                !StringUtils.isBlank(creditorAccount.getAccountHolderName());
    }
}
