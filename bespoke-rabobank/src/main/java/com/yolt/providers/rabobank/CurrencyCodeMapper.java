package com.yolt.providers.rabobank;

import nl.ing.lovebird.extendeddata.common.CurrencyCode;

public interface CurrencyCodeMapper {
    CurrencyCode toCurrencyCode(String currencyCode);
}
