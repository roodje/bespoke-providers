package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamrogroup.common.pis.InitiatePaymentResponseDTO;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.springframework.util.StringUtils;

public class AbnAmroInitiatePaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<InitiatePaymentResponseDTO> {

    @Override
    public void validate(InitiatePaymentResponseDTO initiatePaymentResponseDTO, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        if (initiatePaymentResponseDTO == null) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing response initiation object");
        }

        if (StringUtils.isEmpty(initiatePaymentResponseDTO.getTransactionId())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Missing transaction ID");
        }
    }
}
