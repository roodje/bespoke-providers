package com.yolt.providers.triodosbank.common.mapper;

import com.yolt.providers.triodosbank.common.model.Account;
import com.yolt.providers.triodosbank.common.model.Balance;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class TriodosBankAccountMapper {

    private final Clock clock;
    private TriodosBankBalanceMapper balanceMapper;

    public ProviderAccountDTO mapProviderAccountDTO(Account account,
                                                    List<Balance> balances,
                                                    List<ProviderTransactionDTO> transactions) {

        List<Balance> balancesFiltered = balances.stream()
                .filter(TriodosBankBalanceMapper::excludeNullBalances)
                .collect(Collectors.toList());

        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(account.getName())
                .availableBalance(toAvailableBalanceAmount(balancesFiltered, account))
                .currentBalance(toCurrentBalanceAmount(balancesFiltered, account))
                .yoltAccountType(toAccountType(account))
                .lastRefreshed(ZonedDateTime.now(clock))
                .accountNumber(mapProviderAccountNumberDTO(account))
                .currency(toCurrencyCode(account.getCurrency()))
                .transactions(transactions)
                .extendedAccount(mapExtendedAccountDTO(account, balancesFiltered))
                .build();
    }

    private BigDecimal toAvailableBalanceAmount(List<Balance> balances, Account account) {
        return balanceMapper.findBalanceAmount(balances, account.getCurrency(), BalanceType.EXPECTED.getName());
    }

    private BigDecimal toCurrentBalanceAmount(List<Balance> balances, Account account) {
        return balanceMapper.findBalanceAmount(balances, account.getCurrency(), BalanceType.CLOSING_BOOKED.getName());
    }

    private AccountType toAccountType(Account account) {
        if ("CACC".equals(account.getCashAccountType())) {
            return AccountType.CURRENT_ACCOUNT;
        }
        log.warn("Unsupported cash account type: " + account.getCashAccountType()); //NOSHERIFF CashAccountType is not sensitive
        return null;
    }

    private static ProviderAccountNumberDTO mapProviderAccountNumberDTO(Account account) {
        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
        providerAccountNumberDTO.setHolderName(account.getName());
        return providerAccountNumberDTO;
    }

    private ExtendedAccountDTO mapExtendedAccountDTO(Account account,
                                                     List<Balance> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(toAccountReferences(account))
                .balances(balanceMapper.mapToBalancesDTO(balances))
                .currency(toCurrencyCode(account.getCurrency()))
                .name(account.getName())
                .build();
    }

    private CurrencyCode toCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private List<AccountReferenceDTO> toAccountReferences(Account account) {
        return Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban()));
    }
}
