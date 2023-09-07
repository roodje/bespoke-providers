package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import org.springframework.util.StringUtils;

public class GenericPaymentStatusResponseBodyValidator implements PaymentExecutionResponseBodyValidator<PaymentStatusResponse> {

    @Override
    public void validate(PaymentStatusResponse paymentStatusResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
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