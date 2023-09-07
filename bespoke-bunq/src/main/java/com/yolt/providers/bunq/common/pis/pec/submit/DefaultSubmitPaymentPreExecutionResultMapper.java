package com.yolt.providers.bunq.common.pis.pec.submit;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.beanconfig.BunqDetailsProvider;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqPisHttpClientFactory;
import com.yolt.providers.bunq.common.model.Psd2SessionResponse;
import com.yolt.providers.bunq.common.pis.pec.PaymentProviderState;
import com.yolt.providers.bunq.common.pis.pec.exception.PreExecutionMapperException;
import com.yolt.providers.bunq.common.pis.pec.session.Psd2SessionService;
import com.yolt.providers.bunq.common.pis.pec.submitandstatus.DefaultSubmitAndStatusPaymentPreExecutionResult;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class DefaultSubmitPaymentPreExecutionResultMapper implements SepaSubmitPaymentPreExecutionResultMapper<DefaultSubmitAndStatusPaymentPreExecutionResult> {

    private static final String ACCESS_TOKEN_EXCHANGE_FORMAT = "?grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s";

    private final BunqPisHttpClientFactory httpClientFactory;
    private final ObjectMapper objectMapper;
    private final BunqProperties properties;
    private final Psd2SessionService sessionService;
    private final Clock clock;

    @Override
    public DefaultSubmitAndStatusPaymentPreExecutionResult map(SubmitPaymentRequest submitPaymentRequest) {
        var authMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans(), BunqDetailsProvider.BUNQ_PROVIDER_IDENTIFIER);
        var httpClient = httpClientFactory.createHttpClient(submitPaymentRequest.getRestTemplateManager(), BunqDetailsProvider.BUNQ_PROVIDER_DISPLAY_NAME);

        try {
            Map<String, String> urlQueryParameters = UriComponentsBuilder
                    .fromUriString(submitPaymentRequest.getRedirectUrlPostedBackFromSite())
                    .build()
                    .getQueryParams()
                    .toSingleValueMap();
            String authorizationCode = urlQueryParameters.get("code");
            if (authorizationCode == null) {
                throw new MissingDataException("Missing authorization code in redirect url query parameters");
            }

            var redirectUriWithoutQueryParams = submitPaymentRequest.getRedirectUrlPostedBackFromSite().substring(0, submitPaymentRequest.getRedirectUrlPostedBackFromSite().indexOf('?'));
            String oauthTokenUrl = properties.getOauthTokenUrl() + String.format(ACCESS_TOKEN_EXCHANGE_FORMAT, authorizationCode, redirectUriWithoutQueryParams, authMeans.getClientId(), authMeans.getClientSecret());
            httpClient.exchangeAuthorizationCodeForAccessToken(oauthTokenUrl);
        } catch (TokenInvalidException | MissingDataException e) {
            throw new PreExecutionMapperException("Error occurred during preparing PreExecutionResult", e);
        }

        var providerState = deserializeProviderState(submitPaymentRequest.getProviderState());
        var keyPair = SecurityUtils.createKeyPairFromFormattedStrings(providerState.getKeyPairPublic(), providerState.getKeyPairPrivate());
        var sessionToken = providerState.getSessionToken();
        var expirationTime = providerState.getExpirationTime();
        if (Instant.ofEpochMilli(expirationTime).isBefore(Instant.now(clock))) {
            keyPair = SecurityUtils.generateKeyPair();
            Psd2SessionResponse psd2SessionResponse = null;
            try {
                psd2SessionResponse = sessionService.createSession(httpClient, keyPair, authMeans.getPsd2apiKey());
            } catch (JsonProcessingException | TokenInvalidException e) {
                throw new PreExecutionMapperException("Error occurred during preparing PreExecutionResult", e);
            }
            sessionToken = psd2SessionResponse.getToken().getTokenString();
            expirationTime = Instant.now(clock).plusSeconds(psd2SessionResponse.getExpiryTimeInSeconds()).toEpochMilli();
        }

        return new DefaultSubmitAndStatusPaymentPreExecutionResult(httpClient, providerState.getPaymentId(), authMeans.getPsd2UserId(), sessionToken, expirationTime, keyPair);
    }

    private PaymentProviderState deserializeProviderState(String providerState) {
        try {
            return objectMapper.readValue(Objects.requireNonNull(providerState), PaymentProviderState.class);
        } catch (JsonProcessingException e) {
            throw PaymentExecutionTechnicalException.paymentSubmissionException(e);
        }
    }
}
