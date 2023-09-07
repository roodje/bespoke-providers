package com.yolt.providers.cbiglobe.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.pis.pec.exception.ProviderStateSerializationException;
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
class CbiGlobePaymentProviderStateSerializerTest {

    @InjectMocks
    private CbiGlobePaymentProviderStateSerializer subject;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnProviderStateAsStringWhenCorrectDataAreProvided() throws JsonProcessingException {
        // given
        CbiGlobePaymentProviderState providerState = new CbiGlobePaymentProviderState("paymentId");

        given(objectMapper.writeValueAsString(any(CbiGlobePaymentProviderState.class)))
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
        CbiGlobePaymentProviderState providerState = new CbiGlobePaymentProviderState("paymentId");

        given(objectMapper.writeValueAsString(any(CbiGlobePaymentProviderState.class)))
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