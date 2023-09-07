package com.yolt.providers.starlingbank.common.paymentexecutioncontext.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.starlingbank.common.model.PaymentStatusResponse;

public class StarlingBankStatusPaymentExecutionContextResponseBodyValidator implements PaymentExecutionResponseBodyValidator<PaymentStatusResponse> {

    @Override
    public void validate(PaymentStatusResponse responseBody, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (responseBody == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Response body is missing");
        }

        if (responseBody.getPayments() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing Payments list");
        }

        if (responseBody.getPayments().get(0) == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing Payment in payment's list");
        }

        if (responseBody.getPayments().get(0).getPaymentStatusDetails() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing Payment status details");
        }
    }
}

