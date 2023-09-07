package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.common.util.CbiGlobeDateUtil;
import com.yolt.providers.cbiglobe.dto.ReadAccountBalanceResponseTypeBalances;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CbiGlobeBalanceMapperV2 implements CbiGlobeBalanceMapper {

    private final CurrencyCodeMapper currencyMapper;

    @Override
    public BigDecimal extractBalanceAmount(List<BalanceDTO> balances,
                                           CurrencyCode supportedCurrency,
                                           List<BalanceType> preferredBalanceTypes) {
        if (balances.isEmpty()) {
            return null;
        }
        List<BalanceDTO> supportedBalances = balances.stream()
                .filter(balance -> supportedCurrency.equals(balance.getBalanceAmount().getCurrency()))
                .collect(Collectors.toList());

        if (supportedBalances.isEmpty()) {
            return null;
        }
        if (supportedBalances.size() == 1) {
            return supportedBalances.get(0).getBalanceAmount().getAmount();
        }
        List<BalanceDTO> preferredBalances = supportedBalances.stream()
                .filter(balance -> preferredBalanceTypes.contains(balance.getBalanceType()))
                .collect(Collectors.toList());

        if (preferredBalances.size() == 1) {
            return preferredBalances.get(0).getBalanceAmount().getAmount();
        }
        for (BalanceType preferredBalanceType : preferredBalanceTypes) {
            for (BalanceDTO balance : preferredBalances) {

                if (preferredBalanceType.equals(balance.getBalanceType())) {
                    return balance.getBalanceAmount().getAmount();
                }
            }
        }

        return null;
    }

    @Override
    public BalanceDTO mapToBalanceDTO(ReadAccountBalanceResponseTypeBalances balance) {
        return BalanceDTO.builder()
                .balanceType(BalanceType.fromName(balance.getBalanceType()))
                .balanceAmount(BalanceAmountDTO.builder()
                        .currency(currencyMapper.toCurrencyCode(balance.getBalanceAmount().getCurrency()))
                        .amount(new BigDecimal(balance.getBalanceAmount().getAmount()))
                        .build())
                .lastChangeDateTime(Optional.ofNullable(balance.getLastChangeDateTime())
                        .map(CbiGlobeDateUtil::dateTimeToZonedDateTime)
                        .orElse(null))
                .referenceDate(Optional.ofNullable(balance.getReferenceDate())
                        .map(CbiGlobeDateUtil::dateToZonedDateTime)
                        .orElse(null))
                .lastCommittedTransaction(balance.getLastCommittedTransaction())
                .build();
    }
}
