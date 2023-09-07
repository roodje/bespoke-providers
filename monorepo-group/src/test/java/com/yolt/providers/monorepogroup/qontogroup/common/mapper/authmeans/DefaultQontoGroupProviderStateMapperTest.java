package com.yolt.providers.monorepogroup.qontogroup.common.mapper.authmeans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoGroupProviderState;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth.DefaultQontoGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth.ProviderStateProcessingException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultQontoGroupProviderStateMapperTest {

    @Mock
    ObjectMapper objectMapper;
    @InjectMocks
    DefaultQontoGroupProviderStateMapper providerStateMapper;

    @Test
    void shouldSerializeProviderState() throws JsonProcessingException, ProviderStateProcessingException {
        //given
        var providersState = new QontoGroupProviderState("accessToken", "refreshToken", 1L);
        var expectedSerializedProviderState = "SerializedProviderState";
        given(objectMapper.writeValueAsString(providersState)).willReturn(expectedSerializedProviderState);

        //when
        var result = providerStateMapper.serialize(providersState);

        //then
        assertThat(result).isEqualTo(expectedSerializedProviderState);
    }

    @Test
    void shouldThrowProviderStateProcessingExceptionWhenExceptionOccurredDuringSerialization() throws JsonProcessingException {
        //given
        var providersState = new QontoGroupProviderState("accessToken", "refreshToken", 1L);
        given(objectMapper.writeValueAsString(providersState)).willThrow(JsonProcessingException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> providerStateMapper.serialize(providersState);

        //then
        assertThatExceptionOfType(ProviderStateProcessingException.class)
                .isThrownBy(call)
                .withMessage("Unable to serialize provider state")
                .withCauseExactlyInstanceOf(JsonProcessingException.class);
    }

    @Test
    void shouldDeserializeProviderState() throws ProviderStateProcessingException, JsonProcessingException {
        //given
        var serializedProviderState = "serializedProviderState";
        var expectedProviderState = new QontoGroupProviderState("accessToken", "refreshToken", 1L);
        given(objectMapper.readValue(serializedProviderState, QontoGroupProviderState.class)).willReturn(expectedProviderState);

        //when
        var result = providerStateMapper.deserialize(serializedProviderState);

        //then
        assertThat(result).isEqualTo(expectedProviderState);
    }

    @Test
    void shouldThrowProviderStateProcessingExceptionWhenExceptionOccurredDuringDeserialization() throws JsonProcessingException {
        //given
        var serializedProviderState = "serializedProviderState";
        given(objectMapper.readValue(serializedProviderState, QontoGroupProviderState.class)).willThrow(JsonProcessingException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> providerStateMapper.deserialize(serializedProviderState);

        //then
        assertThatExceptionOfType(ProviderStateProcessingException.class)
                .isThrownBy(call)
                .withMessage("Unable to deserialize provider state")
                .withCauseExactlyInstanceOf(JsonProcessingException.class);
    }
}