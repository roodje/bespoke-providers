package com.yolt.providers.volksbank.common.pis.pec.submit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VolksbankSubmitPaymentPaymentIdExtractorV2Test {

    @InjectMocks
    private VolksbankSubmitPaymentPaymentIdExtractorV2 subject;

    @Test
    void shouldReturnPaymentIdTakenFromPreExecutionResultWhenCorrectDataAreProvided() {
        // given
        var preExecutionResult = preparePreExecutionResult();

        // when
        var result = subject.extractPaymentId(null, preExecutionResult);

        // then
        assertThat(result).isEqualTo("fakePaymentId");
    }

    private VolksbankSepaSubmitPreExecutionResult preparePreExecutionResult() {
        return new VolksbankSepaSubmitPreExecutionResult(
                null,
                null,
                "fakePaymentId");
    }
}