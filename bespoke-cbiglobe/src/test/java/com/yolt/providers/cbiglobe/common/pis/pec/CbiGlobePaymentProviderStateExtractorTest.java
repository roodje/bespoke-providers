package com.yolt.providers.cbiglobe.common.pis.pec;

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
class CbiGlobePaymentProviderStateExtractorTest {

    @InjectMocks
    private CbiGlobePaymentProviderStateExtractor<String, String> subject;

    @Mock
    private CbiGlobePaymentProviderStateSerializer providerStateSerializer;

    @Mock
    private PaymentIdExtractor<String, String> paymentIdExtractor;

    @Captor
    private ArgumentCaptor<CbiGlobePaymentProviderState> providerStateArgumentCaptor;

    @Test
    void shouldReturnProviderStateAsStringWhenCorrectDataAreProvided() {
        // given
        given(providerStateSerializer.serialize(any(CbiGlobePaymentProviderState.class)))
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

        CbiGlobePaymentProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState.getPaymentId()).isEqualTo("paymentId");
        assertThat(result).isEqualTo("providerState");
    }
}