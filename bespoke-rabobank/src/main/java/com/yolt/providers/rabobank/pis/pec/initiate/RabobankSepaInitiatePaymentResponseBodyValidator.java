package com.yolt.providers.rabobank.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;
import org.apache.commons.lang3.StringUtils;

public class RabobankSepaInitiatePaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<InitiatedTransactionResponse> {

    @Override
    public void validate(InitiatedTransactionResponse initiatedTransactionResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (initiatedTransactionResponse == null ||
                initiatedTransactionResponse.getLinks() == null ||
                initiatedTransactionResponse.getLinks().getScaRedirect() == null ||
                StringUtils.isBlank(initiatedTransactionResponse.getLinks().getScaRedirect().getHref())
        ) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing SCA redirect link");
        }
    }
}
