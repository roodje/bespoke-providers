package com.yolt.providers.fineco.pis.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.fineco.dto.PaymentResponse;
import org.apache.commons.lang3.StringUtils;

public class FinecoStatusPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<PaymentResponse> {

    @Override
    public void validate(PaymentResponse paymentStatusResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (paymentStatusResponse == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status response");
        } else if (StringUtils.isBlank(paymentStatusResponse.getTransactionStatus())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status");
        }
    }
}
