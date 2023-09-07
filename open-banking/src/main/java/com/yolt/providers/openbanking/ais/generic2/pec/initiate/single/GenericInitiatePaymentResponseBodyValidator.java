package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5;
import org.springframework.util.StringUtils;

public class GenericInitiatePaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<OBWriteDomesticConsentResponse5> {

    @Override
    public void validate(OBWriteDomesticConsentResponse5 httpResponseBody, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (httpResponseBody == null || httpResponseBody.getData() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Data is missing");
        }

        if (StringUtils.isEmpty(httpResponseBody.getData().getConsentId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Consent ID is missing");
        }

        if (httpResponseBody.getData().getStatus() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Status is missing");
        }
    }
}
