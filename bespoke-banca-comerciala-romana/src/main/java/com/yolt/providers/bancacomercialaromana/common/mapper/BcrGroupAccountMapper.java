package com.yolt.providers.bancacomercialaromana.common.mapper;

import com.yolt.providers.bancacomercialaromana.common.model.fetchdata.Account;
import com.yolt.providers.bancacomercialaromana.common.model.fetchdata.Balance;
import com.yolt.providers.bancacomercialaromana.common.util.BcrGroupDateUtil;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.time.Clock;
import java.util.List;

import static java.util.Collections.singletonList;
import static nl.ing.lovebird.extendeddata.account.BalanceType.AVAILABLE;
import static nl.ing.lovebird.extendeddata.account.BalanceType.EXPECTED;

@AllArgsConstructor
public class BcrGroupAccountMapper {

    private static final String CURRENT_ACCOUNT = "CACC";

    private final BcrGroupBalanceMapper balanceMapper;
    private final Clock clock;

    public ProviderAccountDTO mapProviderAccountDTO(Account account,
                                                    List<Balance> balances,
                                                    List<ProviderTransactionDTO> transactions) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(account.getName())
                .availableBalance(balanceMapper.findBalanceAmount(balances, account.getCurrency(), AVAILABLE))
                .currentBalance(balanceMapper.findBalanceAmount(balances, account.getCurrency(), EXPECTED))
                .yoltAccountType(toAccountType(account))
                .lastRefreshed(BcrGroupDateUtil.getCurrentZoneDateTime(clock))
                .accountNumber(new ProviderAccountNumberDTO(Scheme.IBAN, account.getIban()))
                .currency(balanceMapper.toCurrencyCode(account.getCurrency()))
                .transactions(transactions)
                .extendedAccount(mapExtendedAccountDTO(account, balances))
                .build();
    }

    private AccountType toAccountType(Account account) {
        return CURRENT_ACCOUNT.equals(account.getCashAccountType()) ? AccountType.CURRENT_ACCOUNT : null;
    }

    private ExtendedAccountDTO mapExtendedAccountDTO(Account account, List<Balance> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(balanceMapper.mapToBalancesDTO(balances))
                .currency(balanceMapper.toCurrencyCode(account.getCurrency()))
                .name(account.getName())
                .build();
    }
}
