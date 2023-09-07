package com.yolt.providers.yoltprovider.pis;

import com.yolt.providers.common.exception.PaymentValidationException;
import com.yolt.providers.common.exception.dto.DetailedErrorInformation;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;

import static com.yolt.providers.common.exception.dto.FieldName.DYNAMICFIELDS_DEBTORNAME;

public class YoltBankPaymentRequestBodyValidator {
    private String CORRECT_TESTED_VALUE = "CORRECT_VALUE";

    public void validate(InitiateUkDomesticPaymentRequest request) {
        String testedValue = request.getRequestDTO().getDynamicFields().get("debtorName");
        if (testedValue != null && !CORRECT_TESTED_VALUE.equals(testedValue)) {
            throw new PaymentValidationException(
                    new DetailedErrorInformation(DYNAMICFIELDS_DEBTORNAME, "^" + CORRECT_TESTED_VALUE + "$"));
        }
    }
}
