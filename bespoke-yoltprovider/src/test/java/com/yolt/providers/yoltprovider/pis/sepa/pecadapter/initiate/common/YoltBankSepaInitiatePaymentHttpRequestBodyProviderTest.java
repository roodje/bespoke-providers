package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaInitiatePaymentHttpRequestBodyProviderTest {

    @InjectMocks
    private YoltBankSepaInitiatePaymentHttpRequestBodyProvider httpRequestBodyProvider;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnByteArraysForProvideHttpRequestBody() throws JsonProcessingException {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder().build();
        YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult = new YoltBankSepaInitiatePaymentPreExecutionResult(
                requestDTO,
                null,
                null,
                null,
                null
        );
        byte[] expectedBody = new byte[0];
        given(objectMapper.writeValueAsBytes(requestDTO))
                .willReturn(expectedBody);

        // when
        byte[] result = httpRequestBodyProvider.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result).isEqualTo(expectedBody);
    }

    @Test
    void shouldThrowErrorWhenObjectMappingFails() throws JsonProcessingException {
        //given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder().build();
        YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult = new YoltBankSepaInitiatePaymentPreExecutionResult(
                requestDTO,
                null,
                null,
                null,
                null
        );
        doThrow(new JsonProcessingException("error") {
        }).when(objectMapper).writeValueAsBytes(any());
        //when
        ThrowableAssert.ThrowingCallable callable = () -> httpRequestBodyProvider.provideHttpRequestBody(preExecutionResult);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(JsonProcessingException.class)
                .satisfies(throwable -> assertThat(throwable.getCause().getMessage()).isEqualTo("error"));

    }
}
