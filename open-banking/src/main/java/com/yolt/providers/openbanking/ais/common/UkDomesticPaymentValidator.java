package com.yolt.providers.openbanking.ais.common;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Performs additional validation beside the existing validation constraints defined on {@link InitiateUkDomesticPaymentRequest}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UkDomesticPaymentValidator {

    public static boolean isValid(InitiateUkDomesticPaymentRequest payment) {
        if (payment == null) {
            return false;
        }
        UkAccountDTO creditorAccount = payment.getRequestDTO().getCreditorAccount();
        return creditorAccount.getAccountIdentifierScheme() != null &&
                creditorAccount.getAccountIdentifier() != null &&
                !StringUtils.isBlank(creditorAccount.getAccountHolderName());
    }
}
