package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CbiGlobeInitiatePaymentPaymentIdExtractorTest {

    @InjectMocks
    private CbiGlobeInitiatePaymentPaymentIdExtractor subject;

    @Mock
    PaymentInitiationRequestResponseType initiatePaymentResponse;

    @Test
    void shouldReturnPaymentIdTakenFromResponseWhenCorrectDataAreProvided() {
        // given
        when(initiatePaymentResponse.getPaymentId()).thenReturn("paymentId");

        // when
        String result = subject.extractPaymentId(initiatePaymentResponse, null);

        // then
        assertThat(result).isEqualTo("paymentId");
    }
}