package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency;

import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.util.function.Function;

public class DefaultCurrencyMapper implements Function<String, CurrencyCode> {

    @Override
    public CurrencyCode apply(String currency) {
        if (currency == null) {
            return null;
        }
        try {
            return CurrencyCode.valueOf(currency);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}