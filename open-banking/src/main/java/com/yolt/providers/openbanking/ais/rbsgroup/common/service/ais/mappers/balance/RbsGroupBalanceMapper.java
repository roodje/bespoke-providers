package com.yolt.providers.openbanking.ais.rbsgroup.common.service.ais.mappers.balance;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

public class RbsGroupBalanceMapper extends DefaultBalanceMapper {

    @Override
    public BigDecimal getBalance(String amount, boolean shouldNegate) {
        if (ObjectUtils.isEmpty(amount)) {
            return null;
        }
        DecimalFormat instance = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        instance.setParseBigDecimal(true);

        try {
            BigDecimal value = (BigDecimal) instance.parse(amount);
            return shouldNegate ? value.negate() : value;
        } catch (ParseException e) {
            return null;
        }
    }
}
