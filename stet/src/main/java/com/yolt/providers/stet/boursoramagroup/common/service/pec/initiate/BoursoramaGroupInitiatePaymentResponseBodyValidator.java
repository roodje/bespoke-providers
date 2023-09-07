package com.yolt.providers.stet.boursoramagroup.common.service.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentExecutionResponseBodyValidator;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.stet.generic.dto.payment.response.StetConsentApprovalLink;
import com.yolt.providers.stet.generic.dto.payment.response.StetLinks;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BoursoramaGroupInitiatePaymentResponseBodyValidator implements PaymentExecutionResponseBodyValidator<StetPaymentInitiationResponseDTO> {

    private static final String RESOURCE_ID_QUERY_PARAMETER = "params%5BresourceId%5D";

    @Override
    public void validate(StetPaymentInitiationResponseDTO responseDTO, JsonNode rawResponseBody) throws ResponseBodyValidationException {
        Optional<String> authorizationUrl = Optional.ofNullable(responseDTO)
                .map(StetPaymentInitiationResponseDTO::getLinks)
                .map(StetLinks::getConsentApproval)
                .map(StetConsentApprovalLink::getHref)
                .map(url -> url.replace("\\", ""));

        if (authorizationUrl.isEmpty() || !StringUtils.hasText(authorizationUrl.get())) {
            throw new ResponseBodyValidationException(rawResponseBody, "Authorization URL is missing");
        }

        UriComponents uriComponents;
        try {
            uriComponents = UriComponentsBuilder.fromUriString(authorizationUrl.get()).build();
        } catch (IllegalArgumentException e) {
            throw new ResponseBodyValidationException(rawResponseBody, "Authorization URL is broken or malformed");
        }

        List<String> parameterValues = uriComponents
                .getQueryParams()
                .get(RESOURCE_ID_QUERY_PARAMETER);

        if (CollectionUtils.isEmpty(parameterValues)) {
            List<String> pathSegments = uriComponents.getPathSegments();
            if (CollectionUtils.isEmpty(pathSegments) || "finalisation-virement".equals(pathSegments.get(pathSegments.size() - 1))) {
                throw new ResponseBodyValidationException(rawResponseBody, "Payment ID is missing");
            }
        } else {
            String paymentId = parameterValues.get(0);
            if (!StringUtils.hasText(paymentId)) {
                throw new ResponseBodyValidationException(rawResponseBody, "Payment ID is missing");
            }
        }
    }
}
