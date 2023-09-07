package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode2;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import lombok.AllArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@AllArgsConstructor
public class DefaultBalanceMapper implements BalanceMapper {
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
        BigDecimal value = new BigDecimal(amount);
        return shouldNegate ? value.negate() : value;
    }
}