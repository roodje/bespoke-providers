package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentPaymentIdExtractorTest {

    @InjectMocks
    private DefaultInitiatePaymentPaymentIdExtractor subject;

    @Test
    void shouldReturnPaymentIdTakenFromResponseWhenCorrectDataAreProvided() {
        // given
        InitiatePaymentResponse paymentResponse = new InitiatePaymentResponse(null, "paymentId", null, null);

        // when
        String result = subject.extractPaymentId(paymentResponse, null);

        // then
        assertThat(result).isEqualTo("paymentId");
    }
}