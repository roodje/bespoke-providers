package com.yolt.providers.cbiglobe.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CbiGlobeMapperUtil {

    public static CurrencyCode toCurrencyCode(final String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException iae) {
            throw new UnsupportedOperationException("Unsupported currency code: " + currencyCode);
        }
    }
}
