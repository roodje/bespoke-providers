package com.yolt.providers.openbanking.ais.tsbgroup.common.mapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

public class TsbGroupExtendedAmountMapper extends DefaultBalanceMapper {

    @Override
    public BigDecimal getBalance(String amount, boolean shouldNegate) {
        if (ObjectUtils.isEmpty(amount)) {
            return null;
        }
        DecimalFormat instance = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        instance.setParseBigDecimal(true);
        BigDecimal value;
        try {
            value = (BigDecimal) instance.parse(amount);
            return shouldNegate ? value.negate() : value;
        } catch (ParseException e) {
            return null;
        }
    }
}
