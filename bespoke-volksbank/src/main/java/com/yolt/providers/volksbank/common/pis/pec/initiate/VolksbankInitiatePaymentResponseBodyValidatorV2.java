package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;
import org.springframework.util.StringUtils;

public class VolksbankInitiatePaymentResponseBodyValidatorV2 implements PaymentExecutionResponseBodyValidator<InitiatePaymentResponse> {

    @Override
    public void validate(InitiatePaymentResponse initiatePaymentResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (initiatePaymentResponse == null || StringUtils.isEmpty(initiatePaymentResponse.getPaymentId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Payment ID is missing");
        }
    }
}
