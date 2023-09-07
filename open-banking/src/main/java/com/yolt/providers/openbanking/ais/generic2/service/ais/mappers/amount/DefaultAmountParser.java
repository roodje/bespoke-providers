package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount;

import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.function.Function;

public class DefaultAmountParser implements Function<String, BigDecimal> {
    @Override
    public BigDecimal apply(String amount) {
        if (ObjectUtils.isEmpty(amount)) {
            return null;
        }
        DecimalFormat instance = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        instance.setParseBigDecimal(true);
        try {
            return (BigDecimal) instance.parse(amount);
        } catch (ParseException e) {
            return null;
        }
    }
}
