package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.volksbank.dto.v1_1.PaymentStatus;

public class VolksbankSubmitResponseBodyValidatorV2 implements PaymentExecutionResponseBodyValidator<PaymentStatus> {

    @Override
    public void validate(PaymentStatus paymentStatusResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (paymentStatusResponse == null || paymentStatusResponse.getTransactionStatus() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status");
        }
    }
}
