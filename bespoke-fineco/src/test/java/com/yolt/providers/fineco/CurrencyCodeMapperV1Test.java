package com.yolt.providers.fineco;

import com.yolt.providers.fineco.data.mappers.CurrencyCodeMapper;
import com.yolt.providers.fineco.data.mappers.CurrencyCodeMapperV1;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CurrencyCodeMapperV1Test {

    private CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();

    @Test
    public void shoutMapToCurrencyCode() {
        //given
        String currencyCodeName = CurrencyCode.EUR.name();
        //when
        CurrencyCode currencyCode = currencyCodeMapper.toCurrencyCode(currencyCodeName);
        //then
        assertThat(currencyCode).isEqualTo(CurrencyCode.EUR);
    }

    @Test
    public void shouldReturnNullWhenCannotMapToCurrencyCode() {
        //given
        String currencyCodeName = "Some other currency name";
        //when
        CurrencyCode currencyCode = currencyCodeMapper.toCurrencyCode(currencyCodeName);
        //then
        assertThat(currencyCode).isEqualTo(null);
    }

    @Test
    public void shouldReturnNullWhenCurrencyCodeIsNull() {
        //given
        String currencyCodeName = null;
        //when
        CurrencyCode currencyCode = currencyCodeMapper.toCurrencyCode(currencyCodeName);
        //then
        assertThat(currencyCode).isEqualTo(null);
    }
}
