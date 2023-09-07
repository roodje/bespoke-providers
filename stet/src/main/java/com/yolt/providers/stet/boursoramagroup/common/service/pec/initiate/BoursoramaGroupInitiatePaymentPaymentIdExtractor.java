package com.yolt.providers.stet.boursoramagroup.common.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentPaymentIdExtractor;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RequiredArgsConstructor
public class BoursoramaGroupInitiatePaymentPaymentIdExtractor extends StetInitiatePaymentPaymentIdExtractor {

    private static final String RESOURCE_ID_QUERY_PARAMETER = "params%5BresourceId%5D";

    private final PaymentAuthorizationUrlExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> authorizationUrlExtractor;

    @Override
    public String extractPaymentId(StetPaymentInitiationResponseDTO responseDTO, StetInitiatePreExecutionResult unused) {
        String authorizationUrl = authorizationUrlExtractor.extractAuthorizationUrl(responseDTO, unused);

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString(authorizationUrl)
                .build();

        return extractPaymentIdFromQueryParameterOrPathSegment(uriComponents);
    }

    private String extractPaymentIdFromQueryParameterOrPathSegment(UriComponents uriComponents) {
        List<String> parameterValues = uriComponents
                .getQueryParams()
                .get(RESOURCE_ID_QUERY_PARAMETER);

        return CollectionUtils.isEmpty(parameterValues) ? extractPaymentIdFromPathSegments(uriComponents) : parameterValues.get(0);
    }

    private String extractPaymentIdFromPathSegments(UriComponents uriComponents) {
        List<String> pathSegments = uriComponents.getPathSegments();
        if (CollectionUtils.isEmpty(pathSegments)) {
            return null;
        }
        return pathSegments.get(pathSegments.size() - 1);
    }
}
