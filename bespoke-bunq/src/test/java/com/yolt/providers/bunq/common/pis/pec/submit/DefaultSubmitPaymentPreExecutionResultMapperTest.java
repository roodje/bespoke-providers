package com.yolt.providers.bunq.common.pis.pec.submit;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqPisHttpClient;
import com.yolt.providers.bunq.common.http.BunqPisHttpClientFactory;
import com.yolt.providers.bunq.common.model.Psd2SessionResponse;
import com.yolt.providers.bunq.common.model.Token;
import com.yolt.providers.bunq.common.pis.pec.PaymentProviderState;
import com.yolt.providers.bunq.common.pis.pec.exception.PreExecutionMapperException;
import com.yolt.providers.bunq.common.pis.pec.exception.ProviderStateSerializationException;
import com.yolt.providers.bunq.common.pis.pec.session.Psd2SessionService;
import com.yolt.providers.bunq.common.pis.pec.submitandstatus.DefaultSubmitAndStatusPaymentPreExecutionResult;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultSubmitPaymentPreExecutionResultMapperTest {

    private static final String TOKEN_URL = "https://example.bank.com/oauth/token";
    private static final String FINAL_TOKEN_URL = "https://example.bank.com/oauth/token?grant_type=authorization_code&code=randomCode&redirect_uri=http://example.com/callback&client_id=aabb&client_secret=bbaa";

    private static final String SESSION_TOKEN = "sessionToken";

    @Mock
    BunqPisHttpClientFactory httpClientFactory;
    @Mock
    BunqPisHttpClient httpClient;
    @Mock
    Signer signer;
    @Mock
    RestTemplateManager restTemplateManager;
    @Mock
    BunqProperties properties;
    @Mock
    Psd2SessionService psd2SessionService;

    ObjectMapper objectMapper;

    private DefaultSubmitPaymentPreExecutionResultMapper preExecutionResultMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        preExecutionResultMapper = new DefaultSubmitPaymentPreExecutionResultMapper(httpClientFactory,
                objectMapper,
                properties,
                psd2SessionService,
                Clock.systemUTC());
    }

    @Test
    void shouldReuseSessionAndReturnDefaultSubmitAndStatusPaymentPreExecutionResultWhenCodeParameterIsPresent() throws TokenInvalidException {
        //given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var expectedBunqAuthMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(authMeans, "BUNQ");
        var expirationTime = Instant.now().plusSeconds(600L).toEpochMilli();
        var keyPair = SecurityUtils.generateKeyPair();
        var providerState = new PaymentProviderState(123, PaymentType.SINGLE, SESSION_TOKEN, expirationTime, SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair));
        var submitPaymentRequest = createSubmitPaymentRequest(authMeans, providerState, "http://example.com/callback?code=randomCode&state=randomState");
        when(httpClientFactory.createHttpClient(restTemplateManager, "Bunq")).thenReturn(httpClient);
        when(properties.getOauthTokenUrl()).thenReturn(TOKEN_URL);
        var expectedPreExecutionResult = new DefaultSubmitAndStatusPaymentPreExecutionResult(
                httpClient, 123, expectedBunqAuthMeans.getPsd2UserId(), SESSION_TOKEN, expirationTime, keyPair);

        //when
        DefaultSubmitAndStatusPaymentPreExecutionResult result = preExecutionResultMapper.map(submitPaymentRequest);

        //then
        verify(httpClient, times(1)).exchangeAuthorizationCodeForAccessToken(FINAL_TOKEN_URL);
        assertThat(result).usingRecursiveComparison().ignoringFields("keyPair").isEqualTo(expectedPreExecutionResult);
    }

    @Test
    void shouldCreateNewSessionAndReturnDefaultSubmitAndStatusPaymentPreExecutionResultWhenCodeParameterIsPresent() throws TokenInvalidException, JsonProcessingException {
        //given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var expectedBunqAuthMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(authMeans, "BUNQ");
        var expirationTime = Instant.now().minusSeconds(600L).toEpochMilli();
        var keyPair = SecurityUtils.generateKeyPair();
        var providerState = new PaymentProviderState(123, PaymentType.SINGLE, SESSION_TOKEN, expirationTime, SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair));
        var submitPaymentRequest = createSubmitPaymentRequest(authMeans, providerState, "http://example.com/callback?code=randomCode&state=randomState");
        when(httpClientFactory.createHttpClient(restTemplateManager, "Bunq")).thenReturn(httpClient);
        when(properties.getOauthTokenUrl()).thenReturn(TOKEN_URL);
        var token = mock(Token.class);
        when(token.getTokenString()).thenReturn("tokenId");
        var sessionServerResponse = mock(Psd2SessionResponse.class);
        when(sessionServerResponse.getToken()).thenReturn(token);
        when(sessionServerResponse.getExpiryTimeInSeconds()).thenReturn(600L);
        when(psd2SessionService.createSession(eq(httpClient), any(KeyPair.class), eq(expectedBunqAuthMeans.getPsd2apiKey()))).thenReturn(sessionServerResponse);
        var expectedPreExecutionResult = new DefaultSubmitAndStatusPaymentPreExecutionResult(
                httpClient, 123, expectedBunqAuthMeans.getPsd2UserId(), "tokenId", 1L, keyPair);

        //when
        DefaultSubmitAndStatusPaymentPreExecutionResult result = preExecutionResultMapper.map(submitPaymentRequest);

        //then
        verify(httpClient, times(1)).exchangeAuthorizationCodeForAccessToken(FINAL_TOKEN_URL);
        assertThat(result).usingRecursiveComparison().ignoringFields("keyPair", "expirationTime").isEqualTo(expectedPreExecutionResult);
        assertThat(Instant.ofEpochMilli(result.getExpirationTime())).isAfter(Instant.now().plusSeconds(500));
    }

    @Test
    void shouldReturnPreExecutionMapperExceptionWhenCodeParameterIsMissing() {
        //given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var keyPair = SecurityUtils.generateKeyPair();
        var providerState = new PaymentProviderState(12345, PaymentType.SINGLE, SESSION_TOKEN, 1L, SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair));
        var submitPaymentRequest = createSubmitPaymentRequest(authMeans, providerState, "http://example.com/callback?state=randomState");
        when(httpClientFactory.createHttpClient(restTemplateManager, "Bunq")).thenReturn(httpClient);
        var expectedException = new MissingDataException("Missing authorization code in redirect url query parameters");

        //when
        ThrowableAssert.ThrowingCallable call = () -> preExecutionResultMapper.map(submitPaymentRequest);

        //then
        assertThatExceptionOfType(PreExecutionMapperException.class)
                .isThrownBy(call)
                .withMessage("Error occurred during preparing PreExecutionResult")
                .withCause(expectedException);
    }

    private String serializeProviderState(PaymentProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateSerializationException("Cannot serialize provider state", e);
        }
    }

    private SubmitPaymentRequest createSubmitPaymentRequest(Map<String, BasicAuthenticationMean> authMeans,
                                                            PaymentProviderState providerState,
                                                            String redirectUrlPostedBackFromSite) {
        return new SubmitPaymentRequest(
                serializeProviderState(providerState),
                authMeans,
                redirectUrlPostedBackFromSite,
                signer,
                restTemplateManager,
                "fakePsuIpAddress",
                null
        );
    }
}