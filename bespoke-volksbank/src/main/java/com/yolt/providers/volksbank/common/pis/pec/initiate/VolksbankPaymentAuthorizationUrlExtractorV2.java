package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.volksbank.common.config.VolksbankBaseProperties;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
public class VolksbankPaymentAuthorizationUrlExtractorV2 implements PaymentAuthorizationUrlExtractor<InitiatePaymentResponse, VolksbankSepaInitiatePreExecutionResult> {

    public static final String PAYMENT_ID_PARAM_NAME = "paymentId";
    public static final String PIS_AUTH_SCOPE = "PIS";

    private final VolksbankBaseProperties properties;

    @Override
    public String extractAuthorizationUrl(InitiatePaymentResponse initiatePaymentResponse, VolksbankSepaInitiatePreExecutionResult preExecutionResult) {
        return UriComponentsBuilder.fromHttpUrl(properties.getAuthorizationUrl())
                .queryParam(OAuth.RESPONSE_TYPE, OAuth.CODE)
                .queryParam(OAuth.SCOPE, PIS_AUTH_SCOPE)
                .queryParam(OAuth.STATE, preExecutionResult.getState())
                .queryParam(PAYMENT_ID_PARAM_NAME, initiatePaymentResponse.getPaymentId())
                .queryParam(OAuth.REDIRECT_URI, preExecutionResult.getBaseClientRedirectUrl())
                .queryParam(OAuth.CLIENT_ID, preExecutionResult.getAuthenticationMeans().getClientId())
                .build()
                .toString();
    }
}
