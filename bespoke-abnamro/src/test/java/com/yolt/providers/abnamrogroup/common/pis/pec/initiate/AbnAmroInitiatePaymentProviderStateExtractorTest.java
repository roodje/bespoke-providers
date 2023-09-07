package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.common.pis.InitiatePaymentResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroInitiatePaymentProviderStateExtractorTest {

    @InjectMocks
    private AbnAmroInitiatePaymentProviderStateExtractor subject;

    @Mock
    private AbnAmroProviderStateSerializer providerStateSerializer;

    @Captor
    private ArgumentCaptor<AbnAmroPaymentProviderState> providerStateArgumentCaptor;

    @Test
    void shouldReturnProviderStateForExtractProviderStateWhenCorrectData() {
        // given
        InitiatePaymentResponseDTO initiatePaymentResponseDTO = new InitiatePaymentResponseDTO();
        initiatePaymentResponseDTO.setTransactionId("transactionId");
        AbnAmroInitiatePaymentPreExecutionResult preExecutionResult = new AbnAmroInitiatePaymentPreExecutionResult(
                "",
                null,
                null,
                null,
                "baseClientRedirectUrl",
                "");
        given(providerStateSerializer.serialize(any(AbnAmroPaymentProviderState.class)))
                .willReturn("fakeProviderState");

        // when
        String result = subject.extractProviderState(initiatePaymentResponseDTO, preExecutionResult);

        // then
        then(providerStateSerializer)
                .should()
                .serialize(providerStateArgumentCaptor.capture());
        AbnAmroPaymentProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState.getRedirectUri()).isEqualTo("baseClientRedirectUrl");
        assertThat(capturedProviderState.getTransactionId()).isEqualTo("transactionId");
        assertThat(result).isEqualTo("fakeProviderState");
    }
}