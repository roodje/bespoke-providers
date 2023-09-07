package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.springframework.util.StringUtils;

public class AbnAmroTransactionStatusResponseValidator implements PaymentExecutionResponseBodyValidator<TransactionStatusResponse> {

    @Override
    public void validate(TransactionStatusResponse transactionStatusResponse, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (transactionStatusResponse == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status response");
        }

        if (StringUtils.isEmpty(transactionStatusResponse.getTransactionId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction ID");
        }

        if (transactionStatusResponse.getStatus() == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction status");
        }
    }
}
