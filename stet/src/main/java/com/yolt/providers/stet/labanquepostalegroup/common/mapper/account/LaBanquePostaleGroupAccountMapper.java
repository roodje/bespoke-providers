package com.yolt.providers.stet.labanquepostalegroup.common.mapper.account;

import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LaBanquePostaleGroupAccountMapper extends DefaultAccountMapper {

    public LaBanquePostaleGroupAccountMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return Arrays.asList(StetBalanceType.CLBD, StetBalanceType.XPCD);
    }

    @Override
    protected BigDecimal getBalanceAmount(List<StetBalanceDTO> balances, List<StetBalanceType> preferredBalanceTypes) {
        Map<StetBalanceType, BigDecimal> balanceAmountMap = balances.stream()
                .collect(Collectors.toMap(StetBalanceDTO::getType, StetBalanceDTO::getAmount));

        if (balanceAmountMap.size() == 1) {
            return balanceAmountMap.values().iterator().next();
        }
        for (StetBalanceType preferredBalanceType : preferredBalanceTypes) {
            if (balanceAmountMap.containsKey(preferredBalanceType)) {
                return balanceAmountMap.get(preferredBalanceType);
            }
        }
        return null;
    }
}
