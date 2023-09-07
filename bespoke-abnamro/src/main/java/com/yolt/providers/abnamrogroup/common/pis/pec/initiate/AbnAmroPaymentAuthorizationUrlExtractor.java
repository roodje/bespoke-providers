package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.abnamro.AbnAmroProperties;
import com.yolt.providers.abnamrogroup.common.pis.InitiatePaymentResponseDTO;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
public class AbnAmroPaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<InitiatePaymentResponseDTO, AbnAmroInitiatePaymentPreExecutionResult> {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String FLOW = "flow";
    private static final String PAYMENT_AUTHORIZATION_SEPA_WRITE_READ_SCOPE = "psd2:payment:sepa:write+psd2:payment:sepa:read";

    private final AbnAmroProperties properties;

    @Override
    public String extractAuthorizationUrl(InitiatePaymentResponseDTO initiatePaymentResponseDTO, AbnAmroInitiatePaymentPreExecutionResult preExecutionResult) {
        MultiValueMap<String, String> varMap = new LinkedMultiValueMap<>();
        varMap.add(OAuth.SCOPE, PAYMENT_AUTHORIZATION_SEPA_WRITE_READ_SCOPE);
        varMap.add(OAuth.CLIENT_ID, preExecutionResult.getAuthenticationMeans().getClientId());
        varMap.add(TRANSACTION_ID, initiatePaymentResponseDTO.getTransactionId());
        varMap.add(OAuth.RESPONSE_TYPE, OAuth.CODE);
        varMap.add(FLOW, OAuth.CODE);
        varMap.add(OAuth.REDIRECT_URI, preExecutionResult.getBaseClientRedirectUrl());
        varMap.add(OAuth.STATE, preExecutionResult.getState());
        return UriComponentsBuilder.fromUriString(properties.getOauth2Url()).queryParams(varMap).build().encode().toString();
    }
}
