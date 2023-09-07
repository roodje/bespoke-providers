package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Balance;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.RaiffeisenBalanceType;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupAccountMapper implements RaiffeisenAtGroupAccountMapper {

    private final String providerDisplayName;

    private final RaiffeisenAtGroupDateMapper dateMapper;

    @Override
    public ProviderAccountDTO.ProviderAccountDTOBuilder map(Account account) {
        ProviderAccountNumberDTO accountNumber = null;
        if (StringUtils.hasText(account.getIban())) {
            accountNumber = new ProviderAccountNumberDTO(IBAN, account.getIban());
        }
        List<Balance> balanceList = account.getBalances();

        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(mapToAccountName(providerDisplayName, account))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .availableBalance(findBalanceAmount(balanceList, account.getCurrency(), BalanceType.FORWARD_AVAILABLE))
                .currentBalance(findBalanceAmount(balanceList, account.getCurrency(), BalanceType.INTERIM_AVAILABLE))
                .lastRefreshed(dateMapper.getZonedDateTime())
                .accountNumber(accountNumber)
                .currency(mapToCurrencyCode(account.getCurrency()))
                .bic(account.getBic())
                .extendedAccount(mapExtendedAccountDTO(providerDisplayName, account, account.getBalances()));
    }

    private String mapToAccountName(String providerDisplayName, Account account) {
        if (StringUtils.hasText(account.getName())) {
            return account.getName();
        }
        return providerDisplayName + " Current Account";
    }

    private BigDecimal findBalanceAmount(List<Balance> balances, String currency, BalanceType preferredBalanceType) {
        if (!balances.isEmpty()) {
            List<Balance> balancesWithSameCurrency = balances.stream()
                    .filter(balance -> currency.equals(balance.getCurrency()))
                    .sorted(Comparator.comparing(balance -> Objects.requireNonNull(balance.getReferenceDate())))
                    .collect(Collectors.toList());

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

    private CurrencyCode mapToCurrencyCode(String currency) {
        try {
            return CurrencyCode.valueOf(currency);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private ExtendedAccountDTO mapExtendedAccountDTO(String providerDisplayName, Account account, List<Balance> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .name(mapToAccountName(providerDisplayName, account))
                .accountReferences(singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .bic(account.getBic())
                .balances(mapToBalancesDTO(balances))
                .product(account.getProduct())
                .currency(mapToCurrencyCode(account.getCurrency()))
                .build();
    }

    private List<BalanceDTO> mapToBalancesDTO(List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getAmount())
                                .currency(mapToCurrencyCode(balance.getCurrency()))
                                .build())
                        .balanceType(mapToBalanceType(balance.getBalanceType()))
                        .referenceDate(dateMapper.getZonedDateTime(balance.getReferenceDate()))
                        .build())
                .collect(Collectors.toList());
    }

    private BalanceType mapToBalanceType(RaiffeisenBalanceType balanceType) {
        switch (balanceType) {
            case INTERIM_AVAILABLE:
                return BalanceType.INTERIM_AVAILABLE;
            case FORWARD_AVAILABLE:
                return BalanceType.FORWARD_AVAILABLE;
            default:
                return null;
        }
    }
}
