package com.yolt.providers.rabobank.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.rabobank.dto.external.StatusResponse;

public class RabobankSepaSubmitPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<StatusResponse> {

    @Override
    public void validate(StatusResponse statusResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (statusResponse == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing status response");
        }

        if (statusResponse.getTransactionStatus() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status");
        }
    }
}
