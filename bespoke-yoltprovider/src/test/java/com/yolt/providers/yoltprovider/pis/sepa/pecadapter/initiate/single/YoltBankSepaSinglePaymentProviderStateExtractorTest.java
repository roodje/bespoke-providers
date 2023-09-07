package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.single;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentResponseDTO;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.SepaProviderState;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.SepaInitiatePaymentResponse;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common.YoltBankSepaInitiatePaymentPreExecutionResult;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaSinglePaymentProviderStateExtractorTest {

    @InjectMocks
    private YoltBankSepaSinglePaymentProviderStateExtractor providerStateExtractor;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnProperProviderState() throws JsonProcessingException {
        // given
        YoltBankSepaInitiatePaymentPreExecutionResult mockResult = createExampleResult();
        SepaInitiatePaymentResponse sepaInitiatePaymentResponse = new SepaInitiatePaymentResponse(
                null,
                "",
                null
        );
        String expectedProviderState = "fakeProviderState";
        given(objectMapper.writeValueAsString(new SepaProviderState("", PaymentType.SINGLE)))
                .willReturn(expectedProviderState);

        // when
        String result = providerStateExtractor.extractProviderState(sepaInitiatePaymentResponse, mockResult);

        // then
        assertThat(result).isEqualTo(expectedProviderState);
    }

    @Test
    void shouldThrowErrorWhenObjectMappingFails() throws JsonProcessingException {
        // given
        YoltBankSepaInitiatePaymentPreExecutionResult mockResult = createExampleResult();
        SepaInitiatePaymentResponse sepaInitiatePaymentResponse = new SepaInitiatePaymentResponse(
                null,
                "",
                null
        );
        doThrow(new JsonProcessingException("error") {
        }).when(objectMapper).writeValueAsString(any());
        //when
        ThrowableAssert.ThrowingCallable callable = () -> providerStateExtractor.extractProviderState(sepaInitiatePaymentResponse, mockResult);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(JsonProcessingException.class)
                .satisfies(throwable -> assertThat(throwable.getCause().getMessage()).isEqualTo("error"));
    }

    private YoltBankSepaInitiatePaymentPreExecutionResult createExampleResult() {
        return new YoltBankSepaInitiatePaymentPreExecutionResult(
                SepaInitiatePaymentRequestDTO.builder()
                        .executionDate(LocalDate.now())
                        .build(),
                null,
                null,
                null,
                null);
    }
}
