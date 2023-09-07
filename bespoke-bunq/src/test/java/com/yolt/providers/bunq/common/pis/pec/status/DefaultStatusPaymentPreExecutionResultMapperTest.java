package com.yolt.providers.bunq.common.pis.pec.status;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
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
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultStatusPaymentPreExecutionResultMapperTest {

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
    Psd2SessionService psd2SessionService;

    ObjectMapper objectMapper;

    private DefaultStatusPaymentPreExecutionResultMapper preExecutionResultMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        preExecutionResultMapper = new DefaultStatusPaymentPreExecutionResultMapper(httpClientFactory,
                psd2SessionService,
                objectMapper,
                Clock.systemUTC());
    }

    @Test
    void shouldReuseExistingSessionAndReturnDefaultSubmitAndStatusPaymentPreExecutionResult() throws JsonProcessingException, TokenInvalidException {
        //given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var expectedBunqAuthMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(authMeans, "BUNQ");
        var expirationTime = Instant.now().plusSeconds(600L).toEpochMilli();
        var keyPair = SecurityUtils.generateKeyPair();
        var providerState = new PaymentProviderState(123, PaymentType.SINGLE, SESSION_TOKEN, expirationTime, SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair));
        var statusPaymentRequest = createStatusPaymentRequest(authMeans, providerState);
        when(httpClientFactory.createHttpClient(restTemplateManager, "Bunq")).thenReturn(httpClient);
        var expectedPreExecutionResult = new DefaultSubmitAndStatusPaymentPreExecutionResult(
                httpClient, 123, expectedBunqAuthMeans.getPsd2UserId(), SESSION_TOKEN, expirationTime, keyPair);

        //when
        DefaultSubmitAndStatusPaymentPreExecutionResult result = preExecutionResultMapper.map(statusPaymentRequest);

        //then
        assertThat(result).usingRecursiveComparison().ignoringFields("keyPair").isEqualTo(expectedPreExecutionResult);
    }

    @Test
    void shouldCreateNewSessionAndReturnDefaultSubmitAndStatusPaymentPreExecutionResult() throws JsonProcessingException, TokenInvalidException {
        //given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var expectedBunqAuthMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(authMeans, "BUNQ");
        var keyPair = SecurityUtils.generateKeyPair();
        var providerState = new PaymentProviderState(123, PaymentType.SINGLE, SESSION_TOKEN, Instant.now().minusSeconds(600L).toEpochMilli(), SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair));
        var statusPaymentRequest = createStatusPaymentRequest(authMeans, providerState);
        when(httpClientFactory.createHttpClient(restTemplateManager, "Bunq")).thenReturn(httpClient);
        var token = mock(Token.class);
        when(token.getTokenString()).thenReturn("tokenId");
        var sessionServerResponse = mock(Psd2SessionResponse.class);
        when(sessionServerResponse.getToken()).thenReturn(token);
        when(sessionServerResponse.getExpiryTimeInSeconds()).thenReturn(600L);
        when(psd2SessionService.createSession(eq(httpClient), any(KeyPair.class), eq(expectedBunqAuthMeans.getPsd2apiKey()))).thenReturn(sessionServerResponse);
        var expectedPreExecutionResult = new DefaultSubmitAndStatusPaymentPreExecutionResult(
                httpClient, 123, expectedBunqAuthMeans.getPsd2UserId(), "tokenId", 1L, keyPair);

        //when
        DefaultSubmitAndStatusPaymentPreExecutionResult result = preExecutionResultMapper.map(statusPaymentRequest);

        //then
        assertThat(result).usingRecursiveComparison().ignoringFields("keyPair", "expirationTime").isEqualTo(expectedPreExecutionResult);
        assertThat(Instant.ofEpochMilli(result.getExpirationTime())).isAfter(Instant.now().plusSeconds(500L));
    }

    @Test
    void shouldReturnPreExecutionMapperExceptionWhenTokenInvalidExceptionWasThrownOnSessionCreation() throws TokenInvalidException, JsonProcessingException {
        //given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var expectedBunqAuthMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(authMeans, "BUNQ");
        var keyPair = SecurityUtils.generateKeyPair();
        var providerState = new PaymentProviderState(12345, PaymentType.SINGLE, SESSION_TOKEN, Instant.now().minusSeconds(600L).toEpochMilli(), SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair));
        var statusPaymentRequest = createStatusPaymentRequest(authMeans, providerState);
        when(httpClientFactory.createHttpClient(restTemplateManager, "Bunq")).thenReturn(httpClient);
        var expectedException = new TokenInvalidException("Forbidden");
        when(psd2SessionService.createSession(eq(httpClient), any(KeyPair.class), eq(expectedBunqAuthMeans.getPsd2apiKey()))).thenThrow(expectedException);

        //when
        ThrowableAssert.ThrowingCallable call = () -> preExecutionResultMapper.map(statusPaymentRequest);

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

    private GetStatusRequest createStatusPaymentRequest(Map<String, BasicAuthenticationMean> authMeans, PaymentProviderState providerState) {
        return new GetStatusRequest(
                serializeProviderState(providerState),
                "123",
                authMeans,
                signer,
                restTemplateManager,
                "fakePsuIpAddress",
                null
        );
    }

}