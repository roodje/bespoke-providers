package com.yolt.providers.cbiglobe.common.mapper;

import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyCodeMapperV1Test {

    private CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();

    @Test
    void shoutMapToCurrencyCode() {
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
