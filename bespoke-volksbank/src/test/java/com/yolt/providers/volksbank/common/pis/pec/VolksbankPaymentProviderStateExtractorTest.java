package com.yolt.providers.volksbank.common.pis.pec;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class VolksbankPaymentProviderStateExtractorTest {

    @InjectMocks
    private VolksbankPaymentProviderStateExtractor<String, String> subject;

    @Mock
    private VolksbankPaymentProviderStateSerializer providerStateSerializer;

    @Mock
    private PaymentIdExtractor<String, String> paymentIdExtractor;

    @Captor
    private ArgumentCaptor<VolksbankPaymentProviderState> providerStateArgumentCaptor;

    @Test
    void shouldReturnProviderStateAsStringWhenCorrectDataAreProvided() {
        // given
        given(providerStateSerializer.serialize(any(VolksbankPaymentProviderState.class)))
                .willReturn("providerState");
        given(paymentIdExtractor.extractPaymentId(anyString(), anyString()))
                .willReturn("paymentId");

        // when
        String result = subject.extractProviderState("responseBody", "preExecutionResult");

        // then
        then(providerStateSerializer)
                .should()
                .serialize(providerStateArgumentCaptor.capture());
        then(paymentIdExtractor)
                .should()
                .extractPaymentId("responseBody", "preExecutionResult");

        VolksbankPaymentProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState.getPaymentId()).isEqualTo("paymentId");
        assertThat(result).isEqualTo("providerState");
    }
}