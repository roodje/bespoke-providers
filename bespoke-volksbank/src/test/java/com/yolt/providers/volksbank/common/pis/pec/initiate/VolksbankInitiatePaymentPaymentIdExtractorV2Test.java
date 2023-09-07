package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VolksbankInitiatePaymentPaymentIdExtractorV2Test {

    @InjectMocks
    private VolksbankInitiatePaymentPaymentIdExtractorV2 subject;

    @Test
    void shouldReturnPaymentIdTakenFromResponseWhenCorrectDataAreProvided() {
        // given
        InitiatePaymentResponse initiatePaymentResponse = new InitiatePaymentResponse();
        initiatePaymentResponse.setPaymentId("paymentId");

        // when
        String result = subject.extractPaymentId(initiatePaymentResponse, null);

        // then
        assertThat(result).isEqualTo("paymentId");
    }
}