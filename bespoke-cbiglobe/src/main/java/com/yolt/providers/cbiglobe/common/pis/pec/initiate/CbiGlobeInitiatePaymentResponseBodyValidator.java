package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class CbiGlobeInitiatePaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<PaymentInitiationRequestResponseType> {

    @Override
    public void validate(PaymentInitiationRequestResponseType initiatePaymentResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (ObjectUtils.isEmpty(initiatePaymentResponse)) {
            throw new ResponseBodyValidationException(rawResponseBody, "InitiatePaymentResponse is empty");
        }
        if (StringUtils.isEmpty(initiatePaymentResponse.getPaymentId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Payment ID is missing");
        }
        if (StringUtils.isEmpty(initiatePaymentResponse.getLinks())
            || StringUtils.isEmpty(initiatePaymentResponse.getLinks().getScaRedirect())
            || StringUtils.isEmpty(initiatePaymentResponse.getLinks().getScaRedirect().getHref())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Authorization redirect URL is missing");
        }
    }
}
