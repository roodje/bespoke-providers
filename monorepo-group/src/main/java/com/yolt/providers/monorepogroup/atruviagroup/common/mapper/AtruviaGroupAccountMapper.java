package com.yolt.providers.monorepogroup.atruviagroup.common.mapper;

import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.Balance;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;

@RequiredArgsConstructor
public class AtruviaGroupAccountMapper {

    private static final String CLOSING_BOOKED = "closingBooked";

    private final AtruviaGroupDateConverter dateConverter;

    public ProviderAccountDTO mapProviderAccountDTO(String providerDisplayName,
                                                    Account account,
                                                    List<Balance> balances,
                                                    List<ProviderTransactionDTO> transactions) {
        ProviderAccountNumberDTO accountNumber = null;
        if (StringUtils.hasText(account.getIban())) {
            accountNumber = new ProviderAccountNumberDTO(Scheme.IBAN, account.getIban());
        }
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(mapToAccountName(providerDisplayName, account))
                .yoltAccountType(CURRENT_ACCOUNT)
                .availableBalance(findBalanceAmount(balances, account.getCurrency(), BalanceType.INTERIM_AVAILABLE))
                .currentBalance(findBalanceAmount(balances, account.getCurrency(), BalanceType.CLOSING_BOOKED))
                .lastRefreshed(dateConverter.getDefaultZonedDateTime())
                .accountNumber(accountNumber)
                .currency(mapToCurrencyCode(account.getCurrency()))
                .transactions(transactions)
                .extendedAccount(mapExtendedAccountDTO(providerDisplayName, account, balances))
                .build();
    }

    private BigDecimal findBalanceAmount(List<Balance> balances, String currency, BalanceType preferredBalanceType) {
        if (!balances.isEmpty()) {
            List<Balance> balancesWithSameCurrency = balances.stream()
                    .filter(balance -> currency.equals(balance.getCurrency()))
                    .sorted(Comparator.comparing(balance -> Objects.requireNonNullElse(balance.getReferenceDate(), "")))
                    .toList();

            if (balancesWithSameCurrency.size() == 1) {
                return balancesWithSameCurrency.get(0).getAmount();
            }
            for (Balance balance : balancesWithSameCurrency) {
                if (preferredBalanceType.equals(mapToBalanceType(balance.getBalanceType()))) {
                    return balance.getAmount();
                }
            }
        }
        return null;
    }

    private ExtendedAccountDTO mapExtendedAccountDTO(String providerDisplayName, Account account, List<Balance> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .name(mapToAccountName(providerDisplayName, account))
                .accountReferences(singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(mapToBalancesDTO(balances))
                .currency(mapToCurrencyCode(account.getCurrency()))
                .build();
    }

    private String mapToAccountName(String providerDisplayName, Account account) {
        if (StringUtils.hasText(account.getName())) {
            return account.getName();
        }

        return  providerDisplayName + " Current Account";
    }

    private List<BalanceDTO> mapToBalancesDTO(List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getAmount())
                                .currency(mapToCurrencyCode(balance.getCurrency()))
                                .build())
                        .balanceType(mapToBalanceType(balance.getBalanceType()))
                        .referenceDate(dateConverter.getNullableZonedDateTime(balance.getReferenceDate()))
                        .build())
                .toList();
    }

    private CurrencyCode mapToCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private BalanceType mapToBalanceType(String balanceType) {
        switch (balanceType) {
            case CLOSING_BOOKED:
                return BalanceType.CLOSING_BOOKED;
            default:
                return null;
        }
    }
}
