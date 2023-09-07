package com.yolt.providers.starlingbank.common.paymentexecutioncontext.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;

public class StarlingBankSubmitPaymentExecutionContextResponseBodyValidator implements PaymentExecutionResponseBodyValidator<PaymentSubmissionResponse> {

    @Override
    public void validate(PaymentSubmissionResponse responseBody, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (responseBody == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Response body is missing");
        }

        if (responseBody.getPaymentOrderUid() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing Payment Order UID");
        }
    }
}
