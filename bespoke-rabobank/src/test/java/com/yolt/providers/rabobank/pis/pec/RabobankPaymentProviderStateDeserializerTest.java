package com.yolt.providers.rabobank.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.rabobank.pis.pec.exception.ProviderStateDeserializationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RabobankPaymentProviderStateDeserializerTest {

    @InjectMocks
    private RabobankPaymentProviderStateDeserializer subject;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnProviderStateWhenCorrectDataAreProvided() throws JsonProcessingException {
        // given
        RabobankPaymentProviderState providerState = new RabobankPaymentProviderState("paymentId");
        given(objectMapper.readValue(anyString(), eq(RabobankPaymentProviderState.class)))
                .willReturn(providerState);

        // when
        RabobankPaymentProviderState result = subject.deserialize("providerState");

        // then
        then(objectMapper)
                .should()
                .readValue("providerState", RabobankPaymentProviderState.class);

        assertThat(result).isEqualTo(providerState);
    }

    @Test
    void shouldThrowProviderStateDeserializationExceptionWhenJsonProcessingExceptionOccursDuringProviderStateDeserialization() throws JsonProcessingException {
        // given
        given(objectMapper.readValue(anyString(), eq(RabobankPaymentProviderState.class)))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.deserialize("providerState");

        // then
        assertThatExceptionOfType(ProviderStateDeserializationException.class)
                .isThrownBy(callable)
                .withMessage("Cannot deserialize provider state")
                .withCauseInstanceOf(JsonProcessingException.class);
    }
}