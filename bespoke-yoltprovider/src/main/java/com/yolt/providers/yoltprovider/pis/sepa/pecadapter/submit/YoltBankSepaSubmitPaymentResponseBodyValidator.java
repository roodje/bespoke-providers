package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;

public class YoltBankSepaSubmitPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<SepaPaymentStatusResponse> {

    @Override
    public void validate(SepaPaymentStatusResponse responseBody, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (responseBody == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing response body");
        }
    }
}
