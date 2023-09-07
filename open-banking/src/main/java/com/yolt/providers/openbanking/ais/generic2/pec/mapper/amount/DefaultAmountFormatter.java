package com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DefaultAmountFormatter implements AmountFormatter {

    private static final int MIN_SCALE_LOWER_BOUND = 0;
    private static final int MIN_SCALE_UPPER_BOUND = 5;
    private static final int DEFAULT_MIN_SCALE = 1;
    private static final int MAXIMUM_FRACTION_DIGITS = 5;

    @Override
    public String format(BigDecimal amount) {
        var minScale = (amount.scale() > MIN_SCALE_LOWER_BOUND && amount.scale() < MIN_SCALE_UPPER_BOUND) ? amount.scale() : DEFAULT_MIN_SCALE;
        var numberFormat = NumberFormat.getInstance(Locale.UK);
        numberFormat.setGroupingUsed(false);
        numberFormat.setMinimumFractionDigits(minScale);
        numberFormat.setMaximumFractionDigits(MAXIMUM_FRACTION_DIGITS);
        return numberFormat.format(amount);
    }
}
