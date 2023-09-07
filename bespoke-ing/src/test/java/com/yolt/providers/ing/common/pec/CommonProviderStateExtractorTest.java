package com.yolt.providers.ing.common.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.ing.common.dto.PaymentProviderState;
import com.yolt.providers.ing.common.exception.ProviderStateSerializationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CommonProviderStateExtractorTest {

    @InjectMocks
    private CommonProviderStateExtractor<String, String> subject;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PaymentIdExtractor<String, String> paymentIdExtractor;

    @Captor
    private ArgumentCaptor<PaymentProviderState> providerStateArgumentCaptor;

    @Test
    void shouldReturnProviderStateAsStringWhenCorrectDataAreProvided() throws JsonProcessingException {
        // given
        given(paymentIdExtractor.extractPaymentId(anyString(), anyString()))
                .willReturn("fakePaymentId");
        given(objectMapper.writeValueAsString(any(PaymentProviderState.class)))
                .willReturn("providerState");

        // when
        String result = subject.extractProviderState("responseBody", "preExecutionResult");

        // then
        then(objectMapper)
                .should()
                .writeValueAsString(providerStateArgumentCaptor.capture());

        PaymentProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState.getPaymentId()).isEqualTo("fakePaymentId");
        assertThat(result).isEqualTo("providerState");
    }

    @Test
    void shouldThrowProviderStateSerializationExceptionWhenJsonProcessingExceptionOccursDuringProviderStateSerialization() throws JsonProcessingException {
        // given
        given(paymentIdExtractor.extractPaymentId(anyString(), anyString()))
                .willReturn("fakePaymentId");
        given(objectMapper.writeValueAsString(any(PaymentProviderState.class)))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractProviderState("responseBody", "preExecutionResult");

        // then
        assertThatExceptionOfType(ProviderStateSerializationException.class)
                .isThrownBy(callable)
                .withMessage("Cannot serialize provider state")
                .withCauseInstanceOf(JsonProcessingException.class);
    }
}