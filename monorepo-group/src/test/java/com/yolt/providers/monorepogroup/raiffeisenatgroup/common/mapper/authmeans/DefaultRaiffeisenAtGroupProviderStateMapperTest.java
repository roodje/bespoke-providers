package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.RaiffeisenAtGroupProviderState;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultRaiffeisenAtGroupProviderStateMapperTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DefaultRaiffeisenAtGroupProviderStateMapper providerStateMapper;


    @Test
    void shouldSerializeProviderState() throws JsonProcessingException, ProviderStateProcessingException {
        //given
        var providerState = new RaiffeisenAtGroupProviderState("consentId");
        var expectedSerializedProviderState = "serializedProviderState";
        given(objectMapper.writeValueAsString(providerState)).willReturn(expectedSerializedProviderState);

        //when
        var result = providerStateMapper.serialize(providerState);

        //then
        assertThat(result).isEqualTo(expectedSerializedProviderState);

    }

    @Test
    void shouldThrowProviderStateProcessingExceptionWhenJsonProcessingExceptionIsThrownByObjectMapperDuringSerializing() throws JsonProcessingException {
        //given
        var providerState = new RaiffeisenAtGroupProviderState("consentId");
        given(objectMapper.writeValueAsString(providerState)).willThrow(JsonProcessingException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> providerStateMapper.serialize(providerState);

        //then
        assertThatExceptionOfType(ProviderStateProcessingException.class)
                .isThrownBy(call)
                .withMessage("Unable to serialize provider state");

    }

    @Test
    void shouldDeserializedProviderState() throws JsonProcessingException, ProviderStateProcessingException {
        //given
        var serializedProviderState = "serializedProviderState";
        var expectedDeserializedProviderState = new RaiffeisenAtGroupProviderState("consentId");
        given(objectMapper.readValue(serializedProviderState, RaiffeisenAtGroupProviderState.class)).willReturn(expectedDeserializedProviderState);

        //when
        var result = providerStateMapper.deserialize(serializedProviderState);

        //then
        assertThat(result).isEqualTo(expectedDeserializedProviderState);
    }

    @Test
    void shouldThrowProviderStateProcessingExceptionWhenJsonProcessingExceptionIsThrownByObjectMapperDuringDeserialization() throws JsonProcessingException {
        //given
        var serializedProviderState = "serializedProviderState";
        given(objectMapper.readValue(serializedProviderState, RaiffeisenAtGroupProviderState.class)).willThrow(JsonProcessingException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> providerStateMapper.deserialize(serializedProviderState);

        //then
        assertThatExceptionOfType(ProviderStateProcessingException.class)
                .isThrownBy(call)
                .withMessage("Unable to deserialize provider state");
    }
}