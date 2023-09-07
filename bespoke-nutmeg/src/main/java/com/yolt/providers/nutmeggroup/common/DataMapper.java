package com.yolt.providers.nutmeggroup.common;

import com.yolt.providers.nutmeggroup.common.dto.Pot;
import lombok.experimental.UtilityClass;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

@UtilityClass
public class DataMapper {

    public static ProviderAccountDTO mapToProviderAccountDTO(final Pot pot, final Clock clock) {
        BigDecimal availableBalance = pot.getCurrentValue();

        return ProviderAccountDTO.builder()
                .accountId(pot.getUuid())
                .availableBalance(availableBalance)
                .currentBalance(availableBalance)
                .currency(CurrencyCode.GBP)
                .lastRefreshed(ZonedDateTime.now(clock))
                .name(pot.getName())
                .transactions(new ArrayList<>())
                .extendedAccount(mapToExtendedAccountDTO(pot))
                .yoltAccountType(AccountType.PENSION)
                .build();
    }

    private static ExtendedAccountDTO mapToExtendedAccountDTO(final Pot pot) {
        BalanceAmountDTO balanceAmountDTO = BalanceAmountDTO.builder()
                .amount(pot.getCurrentValue())
                .currency(CurrencyCode.GBP)
                .build();
        BalanceDTO balance = BalanceDTO.builder()
                .balanceAmount(balanceAmountDTO)
                .balanceType(BalanceType.INTERIM_AVAILABLE)
                .build();

        return ExtendedAccountDTO.builder()
                .balances(Collections.singletonList(balance))
                .cashAccountType(ExternalCashAccountType.SAVINGS)
                .currency(CurrencyCode.GBP)
                .name(pot.getName())
                .resourceId(pot.getUuid())
                .build();
    }
}
