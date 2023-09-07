package com.yolt.providers.openbanking.ais.generic2.pec.status.scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import org.apache.commons.lang3.StringUtils;

public class GenericScheduledPaymentStatusResponseBodyValidator implements PaymentExecutionResponseBodyValidator<ScheduledPaymentStatusResponse> {

    @Override
    public void validate(ScheduledPaymentStatusResponse paymentStatusResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (paymentStatusResponse == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Payment status response is null");
        }

        if (paymentStatusResponse.getData() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Payment status response data is null");
        }

        if (paymentStatusResponse.getData().getStatus() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Payment status is null");
        }

        if (StringUtils.isEmpty(paymentStatusResponse.getData().getResourceId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Resource ID is null or empty");
        }
    }
}