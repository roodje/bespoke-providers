package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPisAccessTokenProvider;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateDeserializer;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public class AbnAmroSubmitPaymentPreExecutionResultMapper implements SepaSubmitPaymentPreExecutionResultMapper<AbnAmroSubmitPaymentPreExecutionResult> {

    private final AbnAmroPisAccessTokenProvider pisAccessTokenProvider;
    private final AbnAmroAuthorizationCodeExtractor abnAmroAuthorizationCodeExtractor;
    private final AbnAmroProviderStateDeserializer providerStateDeserializer;

    @Override
    public AbnAmroSubmitPaymentPreExecutionResult map(SubmitPaymentRequest submitPaymentRequest) {
        var authenticationMeans = new AbnAmroAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans());
        var restTemplateManager = submitPaymentRequest.getRestTemplateManager();
        var providerState = providerStateDeserializer.deserialize(submitPaymentRequest.getProviderState());
        var authorizationCode = abnAmroAuthorizationCodeExtractor.extractAuthorizationCode(submitPaymentRequest.getRedirectUrlPostedBackFromSite());
        AccessTokenResponseDTO accessTokenResponseDTO;
        try {
            accessTokenResponseDTO = pisAccessTokenProvider.provideAccessToken(
                    restTemplateManager,
                    authenticationMeans,
                    prepareAuthorizationCodeExchangeRequestBody(authenticationMeans.getClientId(), authorizationCode, providerState.getRedirectUri()));
        } catch (TokenInvalidException e) {
            throw new IllegalStateException("Unable to exchange authorization code for token", e);
        }
        return new AbnAmroSubmitPaymentPreExecutionResult(
                accessTokenResponseDTO,
                authenticationMeans,
                restTemplateManager,
                providerState.getTransactionId()
        );
    }

    private MultiValueMap<String, String> prepareAuthorizationCodeExchangeRequestBody(String clientId, String authorizationCode, String redirectUri) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(OAuth.GRANT_TYPE, OAuth.AUTHORIZATION_CODE);
        body.add(OAuth.CLIENT_ID, clientId);
        body.add(OAuth.CODE, authorizationCode);
        body.add(OAuth.REDIRECT_URI, redirectUri);
        return body;
    }
}
