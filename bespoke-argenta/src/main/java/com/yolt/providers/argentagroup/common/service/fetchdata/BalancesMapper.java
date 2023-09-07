package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.dto.GetBalancesResponse;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BalancesMapper {

    public Map<BalanceType, BalanceDTO> mapBalances(final GetBalancesResponse balancesResponse) {
        Map<BalanceType, BalanceDTO> balanceMap = new HashMap<>();
        balancesResponse.getBalances()
                .forEach(b -> {
                    String balanceTypeNameWithoutUnderscores = b.getBalanceType().toString().replaceAll("_", ""); //TODO C4PO-9178
                    BalanceType bt = BalanceType.fromName(balanceTypeNameWithoutUnderscores);
                    if (bt != null) {
                        BalanceDTO balanceDTO = BalanceDTO.builder()
                                .balanceAmount(
                                        new BalanceAmountDTO(
                                                CurrencyCode.valueOf(b.getBalanceAmount().getCurrency()),
                                                new BigDecimal(b.getBalanceAmount().getAmount())))
                                .balanceType(bt)
                                .referenceDate(LocalDate.parse(b.getReferenceDate(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC))
                                .build();
                        balanceMap.put(bt, balanceDTO);
                    }
                });
        return balanceMap;
    }
}
