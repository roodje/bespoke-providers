package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.extendedtransactionmapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode2;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class HsbcGroupBalanceMapper implements BalanceMapper {
    @Override
    public BigDecimal getBalance(Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType, Supplier<List<OBBalanceType1Code>> balanceListTypeSupplier) {
        return balanceListTypeSupplier.get().stream()
                .filter(balancesByType::containsKey)
                .findFirst()
                .map(type -> mapToBalance(balancesByType, type))
                .orElse(null);
    }

    private BigDecimal mapToBalance(Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType, OBBalanceType1Code type) {
        OBReadBalance1DataBalance balance = balancesByType.get(type);
        return getBalance(balance.getAmount().getAmount(), balance.getCreditDebitIndicator().equals(OBCreditDebitCode2.DEBIT));
    }

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
        } catch (ParseException e) {
            return null;
        }
        return shouldNegate ? value.negate() : value;
    }
}
