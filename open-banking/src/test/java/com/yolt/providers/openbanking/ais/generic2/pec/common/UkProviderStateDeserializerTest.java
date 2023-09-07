package com.yolt.providers.openbanking.ais.generic2.pec.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedUkProviderStateException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UkProviderStateDeserializerTest {

    @InjectMocks
    private UkProviderStateDeserializer subject;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldThrowMalformedUkProviderStateExceptionWhenObjectMapperThrowsJsonProcessingException() throws JsonProcessingException {
        // given
        given(objectMapper.readValue(anyString(), eq(UkProviderState.class)))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.deserialize("state");

        // then
        assertThatExceptionOfType(MalformedUkProviderStateException.class)
                .isThrownBy(callable)
                .withMessage("Unable to parse UK provider state")
                .withCauseInstanceOf(JsonProcessingException.class);
        then(objectMapper)
                .should()
                .readValue("state", UkProviderState.class);
    }
}