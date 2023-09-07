package com.yolt.providers.fineco.data.mappers;

import com.yolt.providers.fineco.v2.dto.Balance;
import com.yolt.providers.fineco.v2.dto.BalanceType;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FinecoBalanceMapperV1 implements FinecoBalanceMapper {
    private static final ZoneId ROME_ZONE_ID = ZoneId.of("Europe/Rome");
    private final CurrencyCodeMapper currencyCodeMapper;

    @Override
    public BigDecimal createBalanceForAccount(List<Balance> balances,
                                              BalanceType balanceType) {
        return balances.stream()
                .filter(balance -> balance.getBalanceType().equals(balanceType))
                .map(balance -> new BigDecimal(balance.getBalanceAmount().getAmount()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<BalanceDTO> mapToBalances(final List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(new BalanceAmountDTO(currencyCodeMapper.toCurrencyCode(balance.getBalanceAmount().getCurrency()),
                                new BigDecimal(balance.getBalanceAmount().getAmount())))
                        .balanceType(nl.ing.lovebird.extendeddata.account.BalanceType.fromName(balance.getBalanceType().name()))
                        .referenceDate(balance.getReferenceDate() == null ? null : balance.getReferenceDate().atStartOfDay(ROME_ZONE_ID))
                        .build()).collect(Collectors.toList());
    }
}
