package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5;
import org.apache.commons.lang3.StringUtils;

public class GenericSubmitScheduledPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<OBWriteDomesticScheduledResponse5> {

    @Override
    public void validate(OBWriteDomesticScheduledResponse5 obWriteDomesticScheduledResponse5, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (obWriteDomesticScheduledResponse5 == null || obWriteDomesticScheduledResponse5.getData() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Data is missing");
        }

        if (StringUtils.isEmpty(obWriteDomesticScheduledResponse5.getData().getDomesticScheduledPaymentId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Domestic Payment ID is missing");
        }

        if (obWriteDomesticScheduledResponse5.getData().getStatus() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Status is missing");
        }
    }
}
