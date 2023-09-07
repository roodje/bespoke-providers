package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;

public class YoltBankSepaInitiatePaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<SepaInitiatePaymentResponse> {

    @Override
    public void validate(SepaInitiatePaymentResponse responseBody, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (responseBody == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing response body");
        }

        if (responseBody.getStatus() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing Payment Status");
        }

        if (responseBody.getScaRedirect() == null || responseBody.getScaRedirect().isBlank()) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing SCA redirect link");
        }
    }
}
