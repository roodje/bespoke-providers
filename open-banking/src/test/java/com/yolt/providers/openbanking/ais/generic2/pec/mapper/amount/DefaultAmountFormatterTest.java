package com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAmountFormatterTest {

    private final DefaultAmountFormatter subject = new DefaultAmountFormatter();

    @Test
    void shouldReturnFormattedAmountWhenCorrectDataAreProvided() {
        // given
        BigDecimal amount = new BigDecimal("1234567.1234567890");

        // when
        String result = subject.format(amount);

        // then
        assertThat(result).isEqualTo("1234567.12346");
    }
}