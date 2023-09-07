package com.yolt.providers.unicredit.common.data.mapper;

import nl.ing.lovebird.extendeddata.common.CurrencyCode;

public interface CurrencyCodeMapper {
    CurrencyCode toCurrencyCode(String currencyCode);
}
