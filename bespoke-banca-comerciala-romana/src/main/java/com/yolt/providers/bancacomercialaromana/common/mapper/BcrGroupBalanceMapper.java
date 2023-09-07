package com.yolt.providers.bancacomercialaromana.common.mapper;

import com.yolt.providers.bancacomercialaromana.common.model.fetchdata.Balance;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.yolt.providers.bancacomercialaromana.common.util.BcrGroupDateUtil.getNullableZonedDateTime;
import static nl.ing.lovebird.extendeddata.account.BalanceType.*;

@AllArgsConstructor
public class BcrGroupBalanceMapper {

    private static final String DATE_FORMAT = "yyyy-mm-dd";

    List<BalanceDTO> mapToBalancesDTO(List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getDecimalAmount())
                                .currency(toCurrencyCode(balance.getCurrency()))
                                .build())
                        .balanceType(toBalanceType(balance.getBalanceType()))
                        .lastChangeDateTime(getNullableZonedDateTime(balance.getReferenceDate(), DATE_FORMAT))
                        .build())
                .collect(Collectors.toList());
    }

    BigDecimal findBalanceAmount(List<Balance> balances, String currency, BalanceType balanceType) {
        if (!balances.isEmpty()) {
            List<Balance> balancesWithSameCurrency = balances.stream()
                    .filter(balance -> currency.equals(balance.getCurrency()))
                    .collect(Collectors.toList());

            if (balancesWithSameCurrency.size() == 1) {
                return balancesWithSameCurrency.get(0).getDecimalAmount();
            }
            for (Balance balance : balancesWithSameCurrency) {
                if (balanceType.equals(toBalanceType(balance.getBalanceType()))) {
                    return balance.getDecimalAmount();
                }
            }
        }
        return null;
    }

    CurrencyCode toCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private BalanceType toBalanceType(String balanceType) {
        switch (balanceType) {
            case "interimAvailable":
                return AVAILABLE;
            case "openingBooked":
                return OPENING_BOOKED;
            case "authorised":
                return AUTHORISED;
            case "closingBooked":
                return CLOSING_BOOKED;
            case "expected":
                return EXPECTED;
            case "forwardAvailable":
                return FORWARD_AVAILABLE;
            default:
                return null;
        }
    }
}
