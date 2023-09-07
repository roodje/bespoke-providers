package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;

public class YoltBankUkDomesticPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<InitiatePaymentConsentResponse> {

    @Override
    public void validate(InitiatePaymentConsentResponse responseBody, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (responseBody == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing response body");
        }

        if (responseBody.getConsentUri() == null || responseBody.getConsentUri().isBlank()) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing consent URI");
        }

        if (responseBody.getPaymentConsent() == null || responseBody.getPaymentConsent().isBlank()) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing payment consent");
        }
    }
}
