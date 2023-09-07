package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.pis;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationCreditorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationDebtorAccount;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class BarclaysUkDomesticPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {

    private static final int END_TO_END_IDENTIFICATION_MAX_LENGTH = 31;
    private static final int NAME_MAX_LENGTH = 18;
    private static final String EXCEPTION_MESSAGE_PATTERN = "%s is too long, maximum allowed for Barclays is %d characters.";
    private static final Pattern REFERENCE_REGEX = Pattern.compile("^(?!\\s)([a-zA-Z0-9-. '()£=\"_,&@#\\/;&+\\u00C0\\u00C1\\u00C2\\u00C3\\u00C4\\u00C5\\u00C6\\u00C7\\u00C8\\u00C9\\u00CA\\u00CB\\u00CC\\u00CD\\u00CE\\u00CF\\u00D0\\u00D1\\u00D2\\u00D3\\u00D4\\u00D5\\u00D6\\u00D8\\u00D9\\u00DA\\u00DB\\u00DC\\u00DD\\u00DE\\u00DF\\u00E0\\u00E1\\u00E2\\u00E3\\u00E4\\u00E5\\u00E6\\u00E7\\u00E8\\u00E9\\u00EA\\u00EB\\u00EC\\u00ED\\u00EE\\u00EF\\u00F0\\u00F1\\u00F2\\u00F3\\u00F4\\u00F5\\u00F6\\u00F8\\u00F9\\u00FA\\u00FB\\u00FC\\u00FD\\u00FE\\u00FF]{1,35})$");

    @Override
    public void validateRequest(final OBWriteDomestic2DataInitiation dataInitiation) {
        OBWriteDomestic2DataInitiationCreditorAccount creditorAccount = dataInitiation.getCreditorAccount();
        OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = dataInitiation.getDebtorAccount();
        if (ObjectUtils.isNotEmpty(debtorAccount) &&
                (debtorAccount.getSchemeName().contains(AccountIdentifierScheme.IBAN.name())
                        || creditorAccount.getSchemeName().contains(AccountIdentifierScheme.IBAN.name()))) {
            throw new IllegalArgumentException("IBAN payments are not supported by Barclays.");
        }
        String creditorAccountName = creditorAccount.getName();
        String debtorAccountName = ObjectUtils.isNotEmpty(debtorAccount) ? debtorAccount.getName() : null;
        if (StringUtils.isEmpty(creditorAccountName)) {
            throw new IllegalArgumentException("Creditor name must be present.");
        }
        if (isLongerThanMax(creditorAccountName)) {
            throw new IllegalArgumentException(String.format(EXCEPTION_MESSAGE_PATTERN, "Creditor name", NAME_MAX_LENGTH));
        }
        if (isLongerThanMax(debtorAccountName)) {
            throw new IllegalArgumentException(String.format(EXCEPTION_MESSAGE_PATTERN, "Debtor name", NAME_MAX_LENGTH));
        }
        if (dataInitiation.getEndToEndIdentification().length() > END_TO_END_IDENTIFICATION_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format(EXCEPTION_MESSAGE_PATTERN, "EndToEndIdentification",
                    END_TO_END_IDENTIFICATION_MAX_LENGTH));
        }
        String reference = dataInitiation.getRemittanceInformation().getReference();
        if (reference != null && !REFERENCE_REGEX.matcher(reference).matches()) {
            throw new IllegalArgumentException("Remittance information contains not allowed characters. It should match " + REFERENCE_REGEX);
        }
    }

    private boolean isLongerThanMax(final String accountName) {
        return StringUtils.isNotBlank(accountName) ? accountName.length() > NAME_MAX_LENGTH : Boolean.FALSE;
    }
}
