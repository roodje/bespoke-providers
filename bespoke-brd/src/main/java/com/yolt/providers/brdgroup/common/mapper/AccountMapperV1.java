package com.yolt.providers.brdgroup.common.mapper;

import com.yolt.providers.brdgroup.common.dto.fetchdata.Account;
import com.yolt.providers.brdgroup.common.dto.fetchdata.Balance;
import com.yolt.providers.brdgroup.common.util.BrdGroupDateConverter;
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
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@RequiredArgsConstructor
public class AccountMapperV1 implements AccountMapper {

    private final BrdGroupDateConverter dateConverter;

    @Override
    public ProviderAccountDTO map(Account account,
                                  String providerDisplayName,
                                  List<Balance> balances,
                                  List<ProviderTransactionDTO> transactions) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(mapToAccountName(providerDisplayName, account))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .availableBalance(findBalanceAmount(balances, account.getCurrency()))
                .currentBalance(findBalanceAmount(balances, account.getCurrency()))
                .lastRefreshed(dateConverter.getDefaultZonedDateTime())
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban()))
                .currency(mapToCurrencyCode(account.getCurrency()))
                .transactions(transactions)
                .extendedAccount(mapExtendedAccountDTO(providerDisplayName, account, balances))
                .build();
    }

    private BigDecimal findBalanceAmount(List<Balance> balances, String currency) {
        if (!balances.isEmpty()) {
            List<Balance> balancesWithSameCurrency = balances.stream()
                    .filter(balance -> currency.equals(balance.getCurrency()))
                    .collect(Collectors.toList());

            if (balancesWithSameCurrency.size() == 1) {
                return balancesWithSameCurrency.get(0).getDecimalAmount();
            }
            for (Balance balance : balancesWithSameCurrency) {
                return balance.getDecimalAmount();
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
        return String.format("%s Current Account", providerDisplayName);
    }

    private List<BalanceDTO> mapToBalancesDTO(List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getDecimalAmount())
                                .currency(mapToCurrencyCode(balance.getCurrency()))
                                .build())
                        .balanceType(BalanceType.EXPECTED)
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
