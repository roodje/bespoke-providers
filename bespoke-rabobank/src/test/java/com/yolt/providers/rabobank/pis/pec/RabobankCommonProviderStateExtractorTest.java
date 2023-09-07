package com.yolt.providers.rabobank.pis.pec;

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
class RabobankCommonProviderStateExtractorTest {

    @InjectMocks
    private RabobankCommonProviderStateExtractor<String, String> subject;

    @Mock
    private RabobankPaymentProviderStateSerializer providerStateSerializer;

    @Mock
    private PaymentIdExtractor<String, String> paymentIdExtractor;

    @Captor
    private ArgumentCaptor<RabobankPaymentProviderState> providerStateArgumentCaptor;

    @Test
    void shouldReturnProviderStateAsStringWhenCorrectDataAreProvided() {
        // given
        given(providerStateSerializer.serialize(any(RabobankPaymentProviderState.class)))
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

        assertThat(providerStateArgumentCaptor.getValue().getPaymentId()).isEqualTo("paymentId");
        assertThat(result).isEqualTo("providerState");
    }
}