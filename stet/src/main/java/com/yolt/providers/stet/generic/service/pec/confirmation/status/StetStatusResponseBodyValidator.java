package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentRequest;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;

import java.util.Optional;

public class StetStatusResponseBodyValidator implements PaymentExecutionResponseBodyValidator<StetPaymentStatusResponseDTO> {

    @Override
    public void validate(StetPaymentStatusResponseDTO responseDTO, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        Optional<StetPaymentStatus> paymentStatus = Optional.ofNullable(responseDTO)
                .map(StetPaymentStatusResponseDTO::getPaymentRequest)
                .map(StetPaymentRequest::getPaymentInformationStatus);

        if (paymentStatus.isEmpty()) {
            throw new ResponseBodyValidationException(rawResponseBody, "Payment status is missing");
        }
    }
}
