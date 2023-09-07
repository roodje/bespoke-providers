package com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.pis;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;


public class BankOfIrelandGroupPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {

    private static final int MAX_REFERENCE_LENGTH = 18;

    @Override
    public void validateRequest(OBWriteDomestic2DataInitiation dataInitiation) {
        if (dataInitiation.getRemittanceInformation().getReference().length() > MAX_REFERENCE_LENGTH) {
            throw new IllegalArgumentException("Remittance information is too long. Maximum allowed length is " + MAX_REFERENCE_LENGTH);
        }

        if (StringUtils.isEmpty(dataInitiation.getEndToEndIdentification())) {
            throw new IllegalArgumentException("End to end identification is required");
        }

        if (ObjectUtils.isEmpty(dataInitiation.getInstructedAmount()) ||
                StringUtils.isEmpty(dataInitiation.getInstructedAmount().getAmount())
                || StringUtils.isEmpty(dataInitiation.getInstructedAmount().getCurrency())) {
            throw new IllegalArgumentException("Amount and currency are required");
        }

        if (ObjectUtils.isEmpty(dataInitiation.getCreditorAccount()) ||
                StringUtils.isEmpty(dataInitiation.getCreditorAccount().getSchemeName()) ||
                StringUtils.isEmpty(dataInitiation.getCreditorAccount().getIdentification()) ||
                StringUtils.isEmpty(dataInitiation.getCreditorAccount().getName())
        ) {
            throw new IllegalArgumentException("Creditor name, account scheme and number are required");
        }

        if (ObjectUtils.isNotEmpty(dataInitiation.getDebtorAccount()) &&
                (StringUtils.isEmpty(dataInitiation.getDebtorAccount().getSchemeName()) ||
                        StringUtils.isEmpty(dataInitiation.getDebtorAccount().getIdentification()))
        ) {
            throw new IllegalArgumentException("Debtor account scheme and number are required, when debtor data is provided");
        }

    }
}
