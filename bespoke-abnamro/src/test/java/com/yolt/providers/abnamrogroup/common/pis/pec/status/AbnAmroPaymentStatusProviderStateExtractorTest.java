package com.yolt.providers.abnamrogroup.common.pis.pec.status;

import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroPaymentStatusProviderStateExtractorTest {

    @InjectMocks
    private AbnAmroPaymentStatusProviderStateExtractor subject;

    @Mock
    private AbnAmroProviderStateSerializer providerStateSerializer;

    @Test
    void shouldReturnProviderStateAsStringWhenCorrectDataAreProvided() {
        // given
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState(null,
                null,
                null);
        AbnAmroPaymentStatusPreExecutionResult preExecutionResult = new AbnAmroPaymentStatusPreExecutionResult(providerState,
                null,
                null);

        given(providerStateSerializer.serialize(any(AbnAmroPaymentProviderState.class)))
                .willReturn("providerState");

        // when
        String result = subject.extractProviderState(null, preExecutionResult);

        // then
        then(providerStateSerializer)
                .should()
                .serialize(providerState);
        assertThat(result).isEqualTo("providerState");
    }
}