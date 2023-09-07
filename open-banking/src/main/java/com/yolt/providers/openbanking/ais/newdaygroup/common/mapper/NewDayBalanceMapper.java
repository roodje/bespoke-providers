package com.yolt.providers.openbanking.ais.newdaygroup.common.mapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@AllArgsConstructor
public class NewDayBalanceMapper extends DefaultBalanceMapper {

    @Override
    public BigDecimal getBalance(Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType,
                                 Supplier<List<OBBalanceType1Code>> balanceListTypeSupplier) {
        return balanceListTypeSupplier.get().stream()
                .filter(balancesByType::containsKey)
                .findFirst()
                .map(type -> mapToBalance(balancesByType, type))
                .orElse(null);
    }

    private BigDecimal mapToBalance(Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType,
                                    OBBalanceType1Code type) {
        OBReadBalance1DataBalance balance = balancesByType.get(type);
        String amountString = balance.getAmount().getAmount();
        BigDecimal amount = new BigDecimal(amountString);
        return getBalance(amountString, amount.compareTo(BigDecimal.ZERO) > 0);
    }

}