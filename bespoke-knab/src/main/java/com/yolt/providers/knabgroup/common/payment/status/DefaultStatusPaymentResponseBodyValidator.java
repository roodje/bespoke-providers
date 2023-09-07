package com.yolt.providers.knabgroup.common.payment.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.knabgroup.common.payment.dto.external.StatusPaymentResponse;
import org.apache.commons.lang3.StringUtils;

public class DefaultStatusPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<StatusPaymentResponse> {

    @Override
    public void validate(final StatusPaymentResponse statusPaymentResponse, final JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (statusPaymentResponse == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status response");
        } else if (StringUtils.isBlank(statusPaymentResponse.getTransactionStatus())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status");
        }
    }
}
