package com.yolt.providers.triodosbank.common.mapper;

import com.yolt.providers.triodosbank.common.model.Balance;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TriodosBankBalanceMapper {

    private final Clock clock;

    BigDecimal findBalanceAmount(List<Balance> balances, String currency, String balanceType) {
        if (!balances.isEmpty()) {
            List<Balance> supportedBalances = getSupportedBalances(balances, currency);
            if (supportedBalances.size() == 1) {
                return toAmount(supportedBalances.get(0));
            }
            for (Balance balance : supportedBalances) {
                if (balanceType.equals(balance.getBalanceType())) {
                    return toAmount(balance);
                }
            }
        }
        return null;
    }

    private List<Balance> getSupportedBalances(List<Balance> balances, String currency) {
        return balances.stream()
                .filter(balance -> currency.equals(balance.getBalanceAmount().getCurrency()))
                .collect(Collectors.toList());
    }

    private BigDecimal toAmount(Balance balance) {
        return new BigDecimal(balance.getBalanceAmount().getAmount());
    }

    List<BalanceDTO> mapToBalancesDTO(List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(toAmount(balance))
                                .currency(toCurrencyCode(balance))
                                .build())
                        .balanceType(BalanceType.fromName(balance.getBalanceType()))
                        .lastChangeDateTime(toNullableZonedDateTime(balance.getReferenceDate()))
                        .build())
                .collect(Collectors.toList());
    }

    private CurrencyCode toCurrencyCode(Balance balance) {
        String currency = balance.getBalanceAmount().getCurrency();
        try {
            return CurrencyCode.valueOf(currency);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private ZonedDateTime toNullableZonedDateTime(String dateTime) {
        if (StringUtils.isNotEmpty(dateTime)) {
            return ZonedDateTime.from(LocalDate.parse(dateTime).atStartOfDay(clock.getZone()));
        }
        return null;
    }

    public static boolean excludeNullBalances(Balance balance) {
        return balance.getBalanceAmount() != null &&
               balance.getBalanceAmount().getAmount() != null &&
               balance.getBalanceAmount().getCurrency() != null;
    }
}
