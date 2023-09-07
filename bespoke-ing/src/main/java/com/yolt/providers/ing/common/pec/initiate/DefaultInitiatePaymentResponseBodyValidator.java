package com.yolt.providers.ing.common.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;
import org.apache.commons.lang3.StringUtils;

public class DefaultInitiatePaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<InitiatePaymentResponse> {

    @Override
    public void validate(final InitiatePaymentResponse response, final JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (response == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing response initiation object");
        } else if (StringUtils.isBlank(response.getPaymentId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction ID");
        } else if (StringUtils.isBlank(response.getTransactionStatus())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status");
        } else if (response.getLinks() == null || response.getLinks().getScaRedirect() == null || StringUtils.isBlank(response.getLinks().getScaRedirect())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing sca redirect url");
        }
    }
}
