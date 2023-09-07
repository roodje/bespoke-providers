package com.yolt.providers.stet.societegeneralegroup.common.service.pec.initiate;

import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentPaymentIdExtractor;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.URISyntaxException;

@RequiredArgsConstructor
public class SocieteGeneraleGroupInitiatePaymentIdExtractor extends StetInitiatePaymentPaymentIdExtractor {

    private final PaymentAuthorizationUrlExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> paymentAuthorizationUrlExtractor;

    @Override
    public String extractPaymentId(StetPaymentInitiationResponseDTO response, StetInitiatePreExecutionResult preExecutionResult) {
        String loginUrl = paymentAuthorizationUrlExtractor.extractAuthorizationUrl(response, preExecutionResult);
        return extractPaymentIdFromAuthorizationUrl(loginUrl);
    }

    private String extractPaymentIdFromAuthorizationUrl(String loginUrl) {
        try {
            String fragment = new URI(loginUrl).getFragment();
            return fragment.contains("?") ? fragment.substring(fragment.lastIndexOf('/') + 1, fragment.indexOf('?')) : fragment.substring(fragment.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            throw new GetLoginInfoUrlFailedException("Payment Request Resource Id not found.");
        }
    }
}
