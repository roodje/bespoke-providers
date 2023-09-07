package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentStatusResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.apache.commons.lang3.ObjectUtils;

public class DefaultSubmitAndStatusPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<PaymentServiceProviderDraftPaymentStatusResponse> {

    @Override
    public void validate(final PaymentServiceProviderDraftPaymentStatusResponse statusPaymentResponse, final JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (statusPaymentResponse == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status response");
        } else if (ObjectUtils.isEmpty(statusPaymentResponse.getStatus())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status");
        }
    }
}
