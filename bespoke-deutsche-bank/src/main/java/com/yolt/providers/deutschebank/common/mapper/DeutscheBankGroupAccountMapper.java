package com.yolt.providers.deutschebank.common.mapper;

import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupDateConverter;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.Account;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.Balance;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@RequiredArgsConstructor
public class DeutscheBankGroupAccountMapper {

    private static final String CURRENT_ACCOUNT = "CACC";

    private final DeutscheBankGroupDateConverter dateConverter;

    public ProviderAccountDTO mapProviderAccountDTO(String providerDisplayName,
                                                    Account account,
                                                    List<Balance> balances) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(mapToAccountName(providerDisplayName, account))
                .yoltAccountType(mapToAccountType(account))
                .availableBalance(findBalanceAmount(balances, account.getCurrency(), BalanceType.INTERIM_AVAILABLE))
                .currentBalance(findBalanceAmount(balances, account.getCurrency(), BalanceType.CLOSING_BOOKED))
                .lastRefreshed(dateConverter.getDefaultZonedDateTime())
                .accountNumber(new ProviderAccountNumberDTO(Scheme.IBAN, account.getIban()))
                .currency(mapToCurrencyCode(account.getCurrency()))
                .transactions(new ArrayList<>())
                .extendedAccount(mapExtendedAccountDTO(providerDisplayName, account, balances))
                .build();
    }

    private AccountType mapToAccountType(Account account) {
        if (Objects.isNull(account.getCashAccountType()) || CURRENT_ACCOUNT.equals(account.getCashAccountType())) {
            return AccountType.CURRENT_ACCOUNT;
        }
        return null;
    }

    private BigDecimal findBalanceAmount(List<Balance> balances, String currency, BalanceType preferredBalanceType) {
        if (!balances.isEmpty()) {
            List<Balance> balancesWithSameCurrency = balances.stream()
                    .filter(balance -> currency.equals(balance.getCurrency()))
                    .collect(Collectors.toList());

            if (balancesWithSameCurrency.size() == 1) {
                return balancesWithSameCurrency.get(0).getDecimalAmount();
            }
            for (Balance balance : balancesWithSameCurrency) {
                if (preferredBalanceType.equals(BalanceType.fromName(balance.getBalanceType()))) {
                    return balance.getDecimalAmount();
                }
            }
        }
        return null;
    }

    private ExtendedAccountDTO mapExtendedAccountDTO(String providerDisplayName, Account account, List<Balance> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .name(mapToAccountName(providerDisplayName, account))
                .accountReferences(singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(mapToBalancesDTO(balances))
                .currency(mapToCurrencyCode(account.getCurrency()))
                .build();
    }

    private String mapToAccountName(String providerDisplayName, Account account) {
        if (StringUtils.isNotEmpty(account.getName())) {
            return account.getName();
        }
        if (Objects.isNull(account.getCashAccountType()) || CURRENT_ACCOUNT.equals(account.getCashAccountType())) {
            return providerDisplayName + " Current Account";
        }
        return null;
    }

    private List<BalanceDTO> mapToBalancesDTO(List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getDecimalAmount())
                                .currency(mapToCurrencyCode(balance.getCurrency()))
                                .build())
                        .balanceType(BalanceType.fromName(balance.getBalanceType()))
                        .lastChangeDateTime(dateConverter.getNullableZonedDateTime(balance.getReferenceDate()))
                        .build())
                .collect(Collectors.toList());
    }

    private CurrencyCode mapToCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

}
