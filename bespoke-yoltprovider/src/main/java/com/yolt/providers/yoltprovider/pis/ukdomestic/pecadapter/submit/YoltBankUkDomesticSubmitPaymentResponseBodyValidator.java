package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.model.PaymentSubmitResponse;

public class YoltBankUkDomesticSubmitPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<PaymentSubmitResponse> {

    @Override
    public void validate(PaymentSubmitResponse responseBody, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (responseBody == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing response body");
        }

        if (responseBody.getData() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing data");
        }

        if (responseBody.getData().getPaymentId() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing UK domestic payment ID");
        }
    }
}
