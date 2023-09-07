package com.yolt.providers.axabanque.common.fetchdata.mapper;

import nl.ing.lovebird.extendeddata.common.CurrencyCode;

public interface CurrencyMapper {

    public CurrencyCode mapToCurrencyCode(String code);
}
