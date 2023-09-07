package com.yolt.providers.openbanking.ais.generic2.pec.status.scheduled;

import com.yolt.providers.openbanking.ais.generic2.pec.status.single.GenericPaymentStatusPreExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenericScheduledPaymentStatusResourceIdExtractorTest {

    @InjectMocks
    private GenericScheduledPaymentStatusResourceIdExtractor subject;

    @Test
    void shouldReturnDomesticPaymentIdWhenDomesticPaymentIdIsProvidedInPreExecutionResult() {
        // given
        GenericPaymentStatusPreExecutionResult preExecutionResult = new GenericPaymentStatusPreExecutionResult(null, null, null, "paymentId", "consentId");

        // when
        String result = subject.extractPaymentId(null, preExecutionResult);

        // then
        assertThat(result).isEqualTo("paymentId");
    }

    @Test
    void shouldReturnEmptyStringWhenDomesticPaymentIdIsNotProvidedInPreExecutionResult() {
        // given
        GenericPaymentStatusPreExecutionResult preExecutionResult = new GenericPaymentStatusPreExecutionResult(null, null, null, null, "consentId");

        // when
        String result = subject.extractPaymentId(null, preExecutionResult);

        // then
        assertThat(result).isEmpty();
    }
}