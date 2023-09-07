package com.yolt.providers.ing.common.pec.submit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSubmitPaymentPaymentIdExtractorTest {

    private DefaultSubmitPaymentPaymentIdExtractor sut;

    @Test
    void shouldExtractPaymentId() {
        // given
        var expectedPaymentId = "fakePaymentId";
        var preExecutionResult = new DefaultSubmitPaymentPreExecutionResult(
                expectedPaymentId,
                null,
                null,
                null,
                null,
                null,
                null
        );

        sut = new DefaultSubmitPaymentPaymentIdExtractor();

        // when
        var result = sut.extractPaymentId(null, preExecutionResult);

        // then
        assertThat(result).isEqualTo(expectedPaymentId);
    }
}