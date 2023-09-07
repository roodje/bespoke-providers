package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.ais.extendedtransactionmapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

public class LloydsBankingGroupExtendedAmountMapper extends DefaultBalanceMapper {

    @Override
    public BigDecimal getBalance(String amount, boolean shouldNegate) {
        DecimalFormat instance = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        instance.setParseBigDecimal(true);
        BigDecimal value;
        if (ObjectUtils.isEmpty(amount)) {
            return null;
        }
        try {
            value = (BigDecimal) instance.parse(amount);
            return shouldNegate ? value.negate() : value;
        } catch (ParseException e) {
            return null;
        }
    }
}
