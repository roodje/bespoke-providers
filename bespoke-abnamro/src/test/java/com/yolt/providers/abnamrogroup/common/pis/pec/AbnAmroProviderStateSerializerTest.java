package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abnamrogroup.common.pis.pec.exception.ProviderStateSerializationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroProviderStateSerializerTest {

    @InjectMocks
    private AbnAmroProviderStateSerializer subject;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnProviderStateAsStringWhenCorrectDataAreProvided() throws JsonProcessingException {
        // given
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState();

        given(objectMapper.writeValueAsString(any(AbnAmroPaymentProviderState.class)))
                .willReturn("providerState");

        // when
        String result = subject.serialize(providerState);

        // then
        then(objectMapper)
                .should()
                .writeValueAsString(providerState);

        assertThat(result).isEqualTo("providerState");
    }

    @Test
    void shouldThrowProviderStateSerializationExceptionWhenJsonProcessingExceptionOccursDuringProviderStateSerialization() throws JsonProcessingException {
        // given
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState();

        given(objectMapper.writeValueAsString(any(AbnAmroPaymentProviderState.class)))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.serialize(providerState);

        // then
        assertThatExceptionOfType(ProviderStateSerializationException.class)
                .isThrownBy(callable)
                .withMessage("Cannot serialize provider state")
                .withCauseInstanceOf(JsonProcessingException.class);
    }
}