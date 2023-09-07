package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.cbiglobe.pis.dto.GetPaymentStatusRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;

public class CbiGlobeSubmitResponseBodyValidator implements PaymentExecutionResponseBodyValidator<GetPaymentStatusRequestResponseType> {

    @Override
    public void validate(GetPaymentStatusRequestResponseType paymentStatusResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (paymentStatusResponse == null || paymentStatusResponse.getTransactionStatus() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status");
        }
    }
}
