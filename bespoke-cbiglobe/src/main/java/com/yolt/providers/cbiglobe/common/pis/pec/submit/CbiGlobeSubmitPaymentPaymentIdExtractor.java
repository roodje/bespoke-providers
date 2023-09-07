package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.yolt.providers.cbiglobe.pis.dto.GetPaymentStatusRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;

public class CbiGlobeSubmitPaymentPaymentIdExtractor implements PaymentIdExtractor<GetPaymentStatusRequestResponseType, CbiGlobeSepaSubmitPreExecutionResult> {

    @Override
    public String extractPaymentId(GetPaymentStatusRequestResponseType paymentStatusResponse, CbiGlobeSepaSubmitPreExecutionResult preExecutionResult) {
        return preExecutionResult.getPaymentId();
    }
}
