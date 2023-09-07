package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abnamrogroup.common.pis.pec.exception.ProviderStateDeserializationException;
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
class AbnAmroProviderStateDeserializerTest {

    @InjectMocks
    private AbnAmroProviderStateDeserializer subject;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnProviderStateWhenCorrectDataAreProvided() throws JsonProcessingException {
        // given
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState();
        given(objectMapper.readValue(anyString(), eq(AbnAmroPaymentProviderState.class)))
                .willReturn(providerState);

        // when
        AbnAmroPaymentProviderState result = subject.deserialize("providerState");

        // then
        then(objectMapper)
                .should()
                .readValue("providerState", AbnAmroPaymentProviderState.class);
        assertThat(result).isEqualTo(providerState);
    }

    @Test
    void shouldThrowProviderStateDeserializationExceptionWhenJsonProcessingExceptionOccursDuringProviderStateDeserialization() throws JsonProcessingException {
        // given
        given(objectMapper.readValue(anyString(), eq(AbnAmroPaymentProviderState.class)))
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