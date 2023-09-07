package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPisAccessTokenProvider;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public class AbnAmroInitiatePaymentPreExecutionResultMapper implements SepaInitiatePaymentPreExecutionResultMapper<AbnAmroInitiatePaymentPreExecutionResult> {

    private static final String PSD_2_PAYMENT_SEPA_WRITE_SCOPE = "psd2:payment:sepa:write";

    private final AbnAmroPisAccessTokenProvider pisAccessTokenProvider;

    @SneakyThrows
    @Override
    public AbnAmroInitiatePaymentPreExecutionResult map(InitiatePaymentRequest initiatePaymentRequest) {
        var authenticationMeans = new AbnAmroAuthenticationMeans(initiatePaymentRequest.getAuthenticationMeans());
        var restTemplateManager = initiatePaymentRequest.getRestTemplateManager();
        var accessTokenResponseDTO = pisAccessTokenProvider.provideAccessToken(
                restTemplateManager,
                authenticationMeans,
                prepareClientCredentialsGrantRequestBody(authenticationMeans.getClientId()));
        return new AbnAmroInitiatePaymentPreExecutionResult(
                accessTokenResponseDTO.getAccessToken(),
                authenticationMeans,
                restTemplateManager,
                initiatePaymentRequest.getRequestDTO(),
                initiatePaymentRequest.getBaseClientRedirectUrl(),
                initiatePaymentRequest.getState()
        );
    }

    private MultiValueMap<String, String> prepareClientCredentialsGrantRequestBody(String clientId) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(OAuth.GRANT_TYPE, OAuth.CLIENT_CREDENTIALS);
        body.add(OAuth.CLIENT_ID, clientId);
        body.add(OAuth.SCOPE, PSD_2_PAYMENT_SEPA_WRITE_SCOPE);
        return body;
    }
}
