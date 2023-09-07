package com.yolt.providers.consorsbankgroup.common.ais.mapper;

import com.yolt.providers.consorsbankgroup.dto.BalanceList;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.consorsbankgroup.consorsbank.ConsorsbankBeanConfig.ZONE_ID;

@AllArgsConstructor
public class DefaultBalanceMapper {

    private final Clock clock;

    public Map<BalanceType, BalanceDTO> mapBalances(final BalanceList balances) {
        Map<BalanceType, BalanceDTO> balanceMap = new HashMap<>();
        balances.forEach(b -> {
            BalanceType bt = BalanceType.fromName(b.getBalanceType().toString());
            if (bt != null) {
                ZonedDateTime lastChangedDT = b.getLastChangeDateTime() != null ? b.getLastChangeDateTime().toZonedDateTime() : ZonedDateTime.now(clock);
                BalanceDTO balanceDTO = BalanceDTO.builder()
                        .balanceAmount(new BalanceAmountDTO(CurrencyCode.valueOf(b.getBalanceAmount().getCurrency()), new BigDecimal(b.getBalanceAmount().getAmount())))
                        .balanceType(bt)
                        .lastChangeDateTime(lastChangedDT)
                        .build();
                balanceMap.put(bt, balanceDTO);
            }
        });
        return balanceMap;
    }
}
