package com.yolt.providers.openbanking.ais.santander.service.ais.mappers;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

public class SantanderBalanceMapper extends DefaultBalanceMapper {

    @Override
    public BigDecimal getBalance(String amount, boolean shouldNegate) {
        if (ObjectUtils.isEmpty(amount)) {
            return null;
        }

        try {
            DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
            decimalFormat.setParseBigDecimal(true);
            BigDecimal value = (BigDecimal) decimalFormat.parse(amount);
            return shouldNegate ? value.negate() : value;
        } catch (ParseException e) {
            return null;
        }
    }
}
