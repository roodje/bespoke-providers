package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentStatusResponse;
import com.yolt.providers.bunq.common.model.PaymentStatusValue;
import com.yolt.providers.bunq.common.pis.pec.PaymentProviderState;
import com.yolt.providers.bunq.common.pis.pec.exception.ProviderStateSerializationException;
import com.yolt.providers.common.pis.common.PaymentType;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultSubmitAndStatusPaymentProviderStateExtractorTest {

    private static final String SESSION_TOKEN = "someSessionToken";

    @Mock
    ObjectMapper objectMapper;

    private DefaultSubmitAndStatusPaymentProviderStateExtractor stateExtractor;

    @BeforeEach
    void setUp() {
        stateExtractor = new DefaultSubmitAndStatusPaymentProviderStateExtractor(PaymentType.SINGLE, objectMapper);
    }

    @Test
    void shouldReturnProviderStateWhenCorrectDataAreProvided() throws JsonProcessingException {
        //given
        var authMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(AuthMeans.prepareAuthMeansV2(), "BUNQ");
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        var expirationTime = Instant.now().plusSeconds(600L).toEpochMilli();
        DefaultSubmitAndStatusPaymentPreExecutionResult preExecutionResult = createDefaultSubmitAndStatusPaymentPreExecutionResult(authMeans, keyPair, expirationTime);
        var paymentResponse = new PaymentServiceProviderDraftPaymentStatusResponse(PaymentStatusValue.ACCEPTED);
        when(objectMapper.writeValueAsString(new PaymentProviderState(123, PaymentType.SINGLE,
                SESSION_TOKEN, expirationTime, SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair))))
                .thenReturn("serializedProviderState");

        //when
        String result = stateExtractor.extractProviderState(paymentResponse, preExecutionResult);

        //then
        assertThat(result).isEqualTo("serializedProviderState");
    }

    @Test
    void shouldThrowProviderStateSerializationExceptionWhenExceptionOccurredDuringSerialization() throws JsonProcessingException {
        //given
        var authMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(AuthMeans.prepareAuthMeansV2(), "BUNQ");
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        var expirationTime = Instant.now().plusSeconds(600L).toEpochMilli();
        DefaultSubmitAndStatusPaymentPreExecutionResult preExecutionResult = createDefaultSubmitAndStatusPaymentPreExecutionResult(authMeans, keyPair, expirationTime);
        var paymentResponse = new PaymentServiceProviderDraftPaymentStatusResponse(PaymentStatusValue.ACCEPTED);
        when(objectMapper.writeValueAsString(new PaymentProviderState(123, PaymentType.SINGLE,
                SESSION_TOKEN, expirationTime, SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair)))).thenThrow(JsonProcessingException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> stateExtractor.extractProviderState(paymentResponse, preExecutionResult);

        //then
        assertThatExceptionOfType(ProviderStateSerializationException.class)
                .isThrownBy(call)
                .withMessage("Cannot serialize provider state")
                .withCauseInstanceOf(JsonProcessingException.class);
    }

    private DefaultSubmitAndStatusPaymentPreExecutionResult createDefaultSubmitAndStatusPaymentPreExecutionResult(BunqAuthenticationMeansV2 authMeans, KeyPair keyPair, long expirationTime) {
        return new DefaultSubmitAndStatusPaymentPreExecutionResult(null, 123, authMeans.getPsd2UserId(), SESSION_TOKEN, expirationTime, keyPair);
    }
}