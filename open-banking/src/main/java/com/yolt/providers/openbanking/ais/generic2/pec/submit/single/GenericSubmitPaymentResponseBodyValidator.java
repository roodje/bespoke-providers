package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5;
import org.springframework.util.StringUtils;

public class GenericSubmitPaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<OBWriteDomesticResponse5> {

    @Override
    public void validate(OBWriteDomesticResponse5 obWriteDomesticResponse5, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (obWriteDomesticResponse5 == null || obWriteDomesticResponse5.getData() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Data is missing");
        }

        if (StringUtils.isEmpty(obWriteDomesticResponse5.getData().getDomesticPaymentId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Domestic Payment ID is missing");
        }

        if (obWriteDomesticResponse5.getData().getStatus() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Status is missing");
        }
    }
}
