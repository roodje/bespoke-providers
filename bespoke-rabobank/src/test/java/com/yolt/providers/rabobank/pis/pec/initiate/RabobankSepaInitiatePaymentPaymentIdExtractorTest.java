package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RabobankSepaInitiatePaymentPaymentIdExtractorTest {

    @InjectMocks
    private RabobankSepaInitiatePaymentPaymentIdExtractor subject;

    @Test
    void shouldReturnPaymentIdTakenFromResponseWhenCorrectDataAreProvided() {
        // given
        UUID paymentId = UUID.randomUUID();
        InitiatedTransactionResponse initiatedTransactionResponse = new InitiatedTransactionResponse();
        initiatedTransactionResponse.setPaymentId(paymentId);

        // when
        String result = subject.extractPaymentId(initiatedTransactionResponse, null);

        // then
        assertThat(result).isEqualTo(paymentId.toString());
    }
}