package com.yolt.providers.rabobank;

import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test class verifies logic for mapping currency codes during fetch data process.
 * <p>
 * Disclaimer: {@link CurrencyCodeMapper} is class which is used for testing
 * <p>
 * Covered flows:
 * - mapping currency code when data is correct
 * - mapping currency code when data is incorrect
 * - mapping currency code when data is missing
 * <p>
 */
class CurrencyCodeMapperV1Test {

    private CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();

    @Test
    void shouldMapToCurrencyCode() {
        //given
        String currencyCodeName = CurrencyCode.EUR.name();
        //when
        CurrencyCode currencyCode = currencyCodeMapper.toCurrencyCode(currencyCodeName);
        //then
        assertThat(currencyCode).isEqualTo(CurrencyCode.EUR);
    }

    @Test
    void shouldReturnNullWhenCannotMapToCurrencyCode() {
        //given
        String currencyCodeName = "Some other currency name";
        //when
        CurrencyCode currencyCode = currencyCodeMapper.toCurrencyCode(currencyCodeName);
        //then
        assertThat(currencyCode).isEqualTo(null);
    }

    @Test
    void shouldReturnNullWhenCurrencyCodeIsNull() {
        //given
        String currencyCodeName = null;
        //when
        CurrencyCode currencyCode = currencyCodeMapper.toCurrencyCode(currencyCodeName);
        //then
        assertThat(currencyCode).isEqualTo(null);
    }
}
