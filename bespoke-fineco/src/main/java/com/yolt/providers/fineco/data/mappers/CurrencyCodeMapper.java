package com.yolt.providers.fineco.data.mappers;

import nl.ing.lovebird.extendeddata.common.CurrencyCode;

public interface CurrencyCodeMapper {
    CurrencyCode toCurrencyCode(String currencyCode);
}
