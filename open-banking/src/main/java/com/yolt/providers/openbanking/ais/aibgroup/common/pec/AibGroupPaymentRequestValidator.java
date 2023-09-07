package com.yolt.providers.openbanking.ais.aibgroup.common.pec;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class AibGroupPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {

    private static final int MAX_REFERENCE_LENGTH = 18;
    private static final int MAX_UNSTRUCTURED_LENGTH = 70;
    private static final int MAX_E2E_LENGTH = 30;
    private static final String VALID_SCHEME = "UK.OBIE.SortCodeAccountNumber";

    @Override
    public void validateRequest(OBWriteDomestic2DataInitiation dataInitiation) {

        if (ObjectUtils.isEmpty(dataInitiation.getEndToEndIdentification()) ||
                (dataInitiation.getEndToEndIdentification().length() > MAX_E2E_LENGTH)) {
            throw new IllegalArgumentException("EndToEndIdentification is required. Maximum allowed length is " + MAX_E2E_LENGTH);
        }

        if (ObjectUtils.isEmpty(dataInitiation.getCreditorAccount()) ||
                StringUtils.isEmpty(dataInitiation.getCreditorAccount().getSchemeName()) ||
                !VALID_SCHEME.equals(dataInitiation.getCreditorAccount().getSchemeName()) ||
                StringUtils.isEmpty(dataInitiation.getCreditorAccount().getIdentification()) ||
                StringUtils.isEmpty(dataInitiation.getCreditorAccount().getName())
        ) {
            throw new IllegalArgumentException("Creditor name, account scheme(SortCodeAccountNumber) and number are required");
        }

        if (ObjectUtils.isEmpty(dataInitiation.getInstructedAmount()) ||
                StringUtils.isEmpty(dataInitiation.getInstructedAmount().getAmount())
                || StringUtils.isEmpty(dataInitiation.getInstructedAmount().getCurrency())) {
            throw new IllegalArgumentException("Amount and currency are required");
        }

        if (ObjectUtils.isNotEmpty(dataInitiation.getDebtorAccount()) &&
                (StringUtils.isEmpty(dataInitiation.getDebtorAccount().getSchemeName()) ||
                        !VALID_SCHEME.equals(dataInitiation.getDebtorAccount().getSchemeName()) ||
                        StringUtils.isEmpty(dataInitiation.getDebtorAccount().getIdentification()))
        ) {
            throw new IllegalArgumentException("Debtor account scheme(SortCodeAccountNumber) and number are required, when debtor data is provided");
        }

        if (ObjectUtils.isNotEmpty(dataInitiation.getRemittanceInformation().getReference()) &&
                (dataInitiation.getRemittanceInformation().getReference().length() > MAX_REFERENCE_LENGTH)) {
            throw new IllegalArgumentException("Remittance information 'Reference' is too long. Maximum allowed length is " + MAX_REFERENCE_LENGTH);
        }

        if (ObjectUtils.isNotEmpty(dataInitiation.getRemittanceInformation().getUnstructured()) &&
                (dataInitiation.getRemittanceInformation().getUnstructured().length() > MAX_UNSTRUCTURED_LENGTH)) {
            throw new IllegalArgumentException("Remittance information 'Unstructured' is too long. Maximum allowed length is " + MAX_UNSTRUCTURED_LENGTH);
        }
    }
}
