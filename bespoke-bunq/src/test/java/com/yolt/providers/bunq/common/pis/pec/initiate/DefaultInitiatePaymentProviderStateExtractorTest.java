package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.model.IdObject;
import com.yolt.providers.bunq.common.model.IdResponse;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentResponse;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentProviderStateExtractorTest {

    private static final String SESSION_TOKEN = "someSessionToken";

    @Mock
    ObjectMapper objectMapper;

    private DefaultInitiatePaymentProviderStateExtractor stateExtractor;

    @BeforeEach
    void setUp() {
        stateExtractor = new DefaultInitiatePaymentProviderStateExtractor(PaymentType.SINGLE, objectMapper);
    }

    @Test
    void shouldReturnProviderStateWhenCorrectDataAreProvided() throws JsonProcessingException {
        //given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        long expirationTime = Instant.now().toEpochMilli();
        DefaultInitiatePaymentPreExecutionResult preExecutionResult = createDefaultInitiatePaymentPreExecutionResult(keyPair, expirationTime);
        List<IdResponse> idResponseList = List.of(new IdResponse(new IdObject(100500)));
        var paymentResponse = new PaymentServiceProviderDraftPaymentResponse(idResponseList);
        when(objectMapper.writeValueAsString(new PaymentProviderState(100500, PaymentType.SINGLE,
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
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        long expirationTime = Instant.now().toEpochMilli();
        DefaultInitiatePaymentPreExecutionResult preExecutionResult = createDefaultInitiatePaymentPreExecutionResult(keyPair, expirationTime);
        List<IdResponse> idResponseList = List.of(new IdResponse(new IdObject(100500)));
        var paymentResponse = new PaymentServiceProviderDraftPaymentResponse(idResponseList);
        when(objectMapper.writeValueAsString(new PaymentProviderState(100500, PaymentType.SINGLE,
                SESSION_TOKEN, expirationTime, SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair)))).thenThrow(JsonProcessingException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> stateExtractor.extractProviderState(paymentResponse, preExecutionResult);

        //then
        assertThatExceptionOfType(ProviderStateSerializationException.class)
                .isThrownBy(call)
                .withMessage("Cannot serialize provider state")
                .withCauseInstanceOf(JsonProcessingException.class);
    }

    private DefaultInitiatePaymentPreExecutionResult createDefaultInitiatePaymentPreExecutionResult(KeyPair keyPair, long expirationTime) {
        BunqAuthenticationMeansV2 bunqAuthenticationMeansV2 = new BunqAuthenticationMeansV2("clientId", "clientSecret", 1L, 1L, null);
        return new DefaultInitiatePaymentPreExecutionResult(null, null, "http://yolt.com/callback",
                "someState", bunqAuthenticationMeansV2.getClientId(), bunqAuthenticationMeansV2.getPsd2UserId(), SESSION_TOKEN, expirationTime, keyPair);
    }
}