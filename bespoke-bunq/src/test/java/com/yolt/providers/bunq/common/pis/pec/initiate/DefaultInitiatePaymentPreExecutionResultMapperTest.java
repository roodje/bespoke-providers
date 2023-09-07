package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.http.BunqPisHttpClient;
import com.yolt.providers.bunq.common.http.BunqPisHttpClientFactory;
import com.yolt.providers.bunq.common.model.Psd2SessionResponse;
import com.yolt.providers.bunq.common.model.Token;
import com.yolt.providers.bunq.common.pis.pec.exception.PreExecutionMapperException;
import com.yolt.providers.bunq.common.pis.pec.session.Psd2SessionService;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
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
class DefaultInitiatePaymentPreExecutionResultMapperTest {

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

    private DefaultInitiatePaymentPreExecutionResultMapper preExecutionResultMapper;

    @BeforeEach
    void setUp() {
        preExecutionResultMapper = new DefaultInitiatePaymentPreExecutionResultMapper(httpClientFactory, psd2SessionService, Clock.systemUTC());
    }

    @Test
    void shouldCreateNewSessionAndReturnDefaultInitiatePaymentPreExecutionResult() throws JsonProcessingException, TokenInvalidException {
        //given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var expectedBunqAuthMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(authMeans, "BUNQ");
        var requestDto = SepaInitiatePaymentRequestDTO.builder().build();
        var initiatePaymentRequest = createInitiatePaymentRequest(authMeans, requestDto);
        when(httpClientFactory.createHttpClient(restTemplateManager, "Bunq")).thenReturn(httpClient);
        var token = mock(Token.class);
        when(token.getTokenString()).thenReturn("tokenId");
        var sessionServerResponse = mock(Psd2SessionResponse.class);
        when(sessionServerResponse.getToken()).thenReturn(token);
        when(sessionServerResponse.getExpiryTimeInSeconds()).thenReturn(600L);

        when(psd2SessionService.createSession(eq(httpClient), any(KeyPair.class), eq(expectedBunqAuthMeans.getPsd2apiKey()))).thenReturn(sessionServerResponse);
        var expectedPreExecutionResult = new DefaultInitiatePaymentPreExecutionResult(
                httpClient, requestDto, "baseClientRedirectUrl", "state", expectedBunqAuthMeans.getClientId(),
                expectedBunqAuthMeans.getPsd2UserId(), "tokenId", 1L, null);

        //when
        DefaultInitiatePaymentPreExecutionResult result = preExecutionResultMapper.map(initiatePaymentRequest);

        //then
        assertThat(result).usingRecursiveComparison().ignoringFields("keyPair", "expirationTime").isEqualTo(expectedPreExecutionResult);
        assertThat(Instant.ofEpochMilli(result.getExpirationTime())).isAfterOrEqualTo(Instant.now().plusSeconds(500L));
    }

    @Test
    void shouldReturnPreExecutionMapperExceptionTokenInvalidExceptionWasThrownOnSessionCreation() throws TokenInvalidException, JsonProcessingException {
        //given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var expectedBunqAuthMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(authMeans, "BUNQ");
        var requestDto = SepaInitiatePaymentRequestDTO.builder().build();
        var initiatePaymentRequest = createInitiatePaymentRequest(authMeans, requestDto);
        when(httpClientFactory.createHttpClient(restTemplateManager, "Bunq")).thenReturn(httpClient);
        var expectedException = new TokenInvalidException("Somme message");
        when(psd2SessionService.createSession(eq(httpClient), any(KeyPair.class), eq(expectedBunqAuthMeans.getPsd2apiKey()))).thenThrow(expectedException);

        //when
        ThrowableAssert.ThrowingCallable call = () -> preExecutionResultMapper.map(initiatePaymentRequest);

        //then
        assertThatExceptionOfType(PreExecutionMapperException.class)
                .isThrownBy(call)
                .withCause(expectedException);
    }

    private InitiatePaymentRequest createInitiatePaymentRequest(Map<String, BasicAuthenticationMean> authMeans, SepaInitiatePaymentRequestDTO requestDTO) {
        return new InitiatePaymentRequest(
                requestDTO,
                "baseClientRedirectUrl",
                "state",
                authMeans,
                signer,
                restTemplateManager,
                "fakePsuIpAddress",
                null
        );
    }
}