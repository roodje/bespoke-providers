package com.yolt.providers.openbanking.ais.monzogroup.common.service.ais.potmapper;

import com.yolt.providers.openbanking.ais.monzogroup.common.dto.Pot;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.account.UsageType;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;

@RequiredArgsConstructor
public class MonzoGroupPotMapperV2 {

    private final Clock clock;

    public ProviderAccountDTO mapToProviderAccount(final Pot pot) {
        BigDecimal balance = new BigDecimal(pot.getBalance().getAmount());
        CurrencyCode currencyCode = toCurrencyCode(pot.getBalance().getCurrency());
        String accountId = pot.getId();
        String name = pot.getName();

        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.SAVINGS_ACCOUNT)
                .currentBalance(balance)
                .availableBalance(balance)
                .lastRefreshed(ZonedDateTime.now(clock))
                .currency(currencyCode)
                .accountId(accountId)
                .transactions(new ArrayList<>())
                .name(name)
                .extendedAccount(mapToExtendedModelAccount(accountId, currencyCode, name))
                .build();
    }

    private CurrencyCode toCurrencyCode(final String currency) {
        for (CurrencyCode currencyCode : CurrencyCode.values()) {
            if (currencyCode.name().equals(currency)) {
                return currencyCode;
            }
        }
        return null;
    }

    private ExtendedAccountDTO mapToExtendedModelAccount(final String accountId,
                                                         final CurrencyCode currencyCode,
                                                         final String name) {
        return ExtendedAccountDTO.builder()
                .cashAccountType(ExternalCashAccountType.SAVINGS)
                .currency(currencyCode)
                .name(name)
                .resourceId(accountId)
                .status(Status.ENABLED)
                .usage(UsageType.PRIVATE)
                .build();
    }
}