package com.yolt.providers.ing.common.pec.submit;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.ing.common.dto.PaymentStatusResponse;
import com.yolt.providers.ing.common.pec.PaymentTypeExtractor;

public class DefaultSubmitPaymentTypeExtractor implements PaymentTypeExtractor<PaymentStatusResponse, DefaultSubmitPaymentPreExecutionResult> {

    @Override
    public PaymentType extractPaymentType(PaymentStatusResponse paymentStatusResponse, DefaultSubmitPaymentPreExecutionResult defaultSubmitPaymentPreExecutionResult) {
        return defaultSubmitPaymentPreExecutionResult.getPaymentType();
    }
}
