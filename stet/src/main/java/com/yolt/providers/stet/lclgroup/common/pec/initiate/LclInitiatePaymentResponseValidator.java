package com.yolt.providers.stet.lclgroup.common.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.stet.generic.dto.payment.response.StetConsentApprovalLink;
import com.yolt.providers.stet.generic.dto.payment.response.StetLinks;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Optional;

@RequiredArgsConstructor
public class LclInitiatePaymentResponseValidator implements PaymentExecutionResponseBodyValidator<StetPaymentInitiationResponseDTO> {

    private final LclGroupInitiatePaymentPaymentIdExtractor paymentIdExtractor;

    public void validate(StetPaymentInitiationResponseDTO responseDTO, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        Optional<String> authorizationUrl = Optional.ofNullable(responseDTO)
                .map(StetPaymentInitiationResponseDTO::getLinks)
                .map(StetLinks::getConsentApproval)
                .map(StetConsentApprovalLink::getHref);

        if (authorizationUrl.isEmpty() || !StringUtils.hasText(authorizationUrl.get())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Authorization URL is missing");
        }

        if (!StringUtils.hasText(paymentIdExtractor.extractPaymentId(null, null))) {
            throw new ResponseBodyValidationException(rawResponseBody, "Payment ID is missing");
        }
    }
}
