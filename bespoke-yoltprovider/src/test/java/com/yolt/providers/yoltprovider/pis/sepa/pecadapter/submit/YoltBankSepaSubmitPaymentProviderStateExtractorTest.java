package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.SepaProviderState;
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
class YoltBankSepaSubmitPaymentProviderStateExtractorTest {

    private static final String PAYMENT_ID = "521731";

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private YoltBankSepaSubmitPaymentProviderStateExtractor paymentStateExtractor;

    @Test
    void shouldReturnProperProviderState() throws JsonProcessingException {
        // given
        SepaPaymentStatusResponse responseDTO = new SepaPaymentStatusResponse(PAYMENT_ID, SepaPaymentStatus.COMPLETED);
        YoltBankSepaSubmitPreExecutionResult preExecutionResult = createYoltBankSepaSubmitPreExecutionResult();

        given(objectMapper.writeValueAsString(new SepaProviderState(PAYMENT_ID, PaymentType.SINGLE)))
                .willReturn("fakeProviderState");

        // when
        String providerState = paymentStateExtractor.extractProviderState(responseDTO, preExecutionResult);

        // then
        assertThat(providerState).isEqualTo("fakeProviderState");
    }

    @Test
    void shouldThrowErrorWhenObjectMappingFails() throws JsonProcessingException {
        // given
        SepaPaymentStatusResponse responseDTO = new SepaPaymentStatusResponse(PAYMENT_ID, SepaPaymentStatus.COMPLETED);
        YoltBankSepaSubmitPreExecutionResult preExecutionResult = createYoltBankSepaSubmitPreExecutionResult();

        doThrow(new JsonProcessingException("error") {})
                .when(objectMapper).writeValueAsString(any());
        //when
        ThrowableAssert.ThrowingCallable callable = () ->
                paymentStateExtractor.extractProviderState(responseDTO, preExecutionResult);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(JsonProcessingException.class)
                .satisfies(throwable -> assertThat(throwable.getCause().getMessage()).isEqualTo("error"));
    }

    private YoltBankSepaSubmitPreExecutionResult createYoltBankSepaSubmitPreExecutionResult() {
        return new YoltBankSepaSubmitPreExecutionResult(
                PAYMENT_ID,
                null,
                null,
                PaymentType.SINGLE);
    }
}
