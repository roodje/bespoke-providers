package com.yolt.providers.ing.common.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.ing.common.dto.PaymentStatusResponse;
import org.apache.commons.lang3.StringUtils;

public class DefaultSubmitPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<PaymentStatusResponse> {

    @Override
    public void validate(final PaymentStatusResponse paymentStatusResponse, final JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (paymentStatusResponse == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status response");
        } else if (StringUtils.isBlank(paymentStatusResponse.getTransactionStatus())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status");
        }
    }
}
