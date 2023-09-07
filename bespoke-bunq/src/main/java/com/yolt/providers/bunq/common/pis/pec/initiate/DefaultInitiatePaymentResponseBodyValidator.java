package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.apache.commons.lang3.ObjectUtils;

public class DefaultInitiatePaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<PaymentServiceProviderDraftPaymentResponse> {

    @Override
    public void validate(PaymentServiceProviderDraftPaymentResponse responseBody, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (ObjectUtils.isEmpty(responseBody) || ObjectUtils.isEmpty(responseBody.getResponse()) || ObjectUtils.isEmpty(responseBody.getResponse().get(0))
                || ObjectUtils.isEmpty(responseBody.getResponse().get(0).getId()) || ObjectUtils.isEmpty(responseBody.getResponse().get(0).getId().getId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Response body doesn't contain paymentId");
        }
    }
}
