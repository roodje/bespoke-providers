package com.yolt.providers.gruppocedacri.common.mapper;

import com.yolt.providers.gruppocedacri.common.dto.fetchdata.Account;
import com.yolt.providers.gruppocedacri.common.dto.fetchdata.Balance;
import com.yolt.providers.gruppocedacri.common.util.GruppoCedacriDateConverter;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static nl.ing.lovebird.extendeddata.account.BalanceType.EXPECTED;
import static nl.ing.lovebird.extendeddata.account.BalanceType.INTERIM_AVAILABLE;

@RequiredArgsConstructor
public class GruppoCedacriAccountMapperV1 implements GruppoCedacriAccountMapper {

    private final GruppoCedacriDateConverter dateConverter;

    @Override
    public ProviderAccountDTO map(Account account,
                                  String providerDisplayName,
                                  List<Balance> balances,
                                  List<ProviderTransactionDTO> transactions) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(mapToAccountName(providerDisplayName, account))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .availableBalance(findBalanceAmount(balances, getPreferredBalanceTypesForAvailableBalance()))
                .currentBalance(findBalanceAmount(balances, getPreferredBalanceTypesForCurrentBalance()))
                .lastRefreshed(dateConverter.getDefaultZonedDateTime())
                .accountNumber(mapToAccountReferences(account))
                .currency(mapToCurrencyCode(account.getCurrency()))
                .transactions(transactions)
                .extendedAccount(mapExtendedAccountDTO(providerDisplayName, account, balances))
                .build();
    }

    private BigDecimal findBalanceAmount(List<Balance> accountBalances,
                                         List<BalanceType> preferredBalances) {
        for (BalanceType preferredBalance : preferredBalances) {
            BigDecimal balanceAmount = findBalanceForPreferredBalance(accountBalances, preferredBalance.getName());
            if (balanceAmount != null) {
                return balanceAmount;
            }
        }
        return null;
    }

    private BigDecimal findBalanceForPreferredBalance(final List<Balance> accountBalances, final String preferredBalance) {
        return accountBalances.stream()
                .filter(balance -> preferredBalance.equalsIgnoreCase(balance.getBalanceType()))
                .findFirst()
                .map(Balance::getDecimalAmount)
                .orElse(null);
    }

    List<BalanceType> getPreferredBalanceTypesForCurrentBalance() {
        return Arrays.asList(INTERIM_AVAILABLE, EXPECTED);
    }

    List<BalanceType> getPreferredBalanceTypesForAvailableBalance() {
        return Arrays.asList(EXPECTED, INTERIM_AVAILABLE);
    }

    private ProviderAccountNumberDTO mapToAccountReferences(Account account) {
        ProviderAccountNumberDTO providerAccountNumberDTO = null;

        if (StringUtils.isNotBlank(account.getIban())) {
            providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
            providerAccountNumberDTO.setHolderName(account.getOwnerName());
        }
        return providerAccountNumberDTO;
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
        return String.format("%s Current Account - %s", providerDisplayName, account.getCurrency());
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
