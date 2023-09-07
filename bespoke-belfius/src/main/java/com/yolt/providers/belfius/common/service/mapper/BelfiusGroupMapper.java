package com.yolt.providers.belfius.common.service.mapper;

import com.yolt.providers.belfius.common.model.ais.Account;
import com.yolt.providers.belfius.common.model.ais.TransactionResponse;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BelfiusGroupMapper {

    private final BelfiusGroupTransactionMapper transactionMapper;
    private final Clock clock;

    public ProviderAccountDTO mapToProviderAccountDTO(Account account, List<TransactionResponse.Transaction> transactions, String logicalId) {

        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .accountId(logicalId)
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban()))
                .currency(toCurrencyCode(account.getCurrency()))
                .currentBalance(account.getBalance())
                .availableBalance(account.getBalance())
                .name(account.getAccountName())
                .extendedAccount(createExtendedAccountDto(account))
                .transactions(transactionMapper.mapTransactions(transactions, account))
                .build();
    }

    private ExtendedAccountDTO createExtendedAccountDto(Account account) {
        BalanceDTO balance = BalanceDTO.builder()
                .balanceAmount(toBalanceAmount(account))
                .balanceType(BalanceType.INTERIM_AVAILABLE)
                .build();

        return ExtendedAccountDTO.builder()
                .resourceId(account.getIban())
                .balances(Collections.singletonList(balance))
                .currency(toCurrencyCode(account.getCurrency()))
                .name(account.getAccountName())
                .build();
    }

    private BalanceAmountDTO toBalanceAmount(Account account) {
        return BalanceAmountDTO.builder()
                .currency(toCurrencyCode(account.getCurrency()))
                .amount(account.getBalance())
                .build();
    }

    private CurrencyCode toCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (RuntimeException iae) {
            return null;
        }
    }
}
