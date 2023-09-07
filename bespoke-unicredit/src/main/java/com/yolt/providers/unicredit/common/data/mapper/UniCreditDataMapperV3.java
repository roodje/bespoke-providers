package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.unicredit.common.dto.UniCreditAccountDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditBalanceDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditTransactionsDTO;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UniCreditDataMapperV3 implements UniCreditDataMapper {

    private final CurrencyCodeMapper currencyCodeMapper;
    private final BalanceMapper balanceMapper;
    private final TransactionMapper transactionMapper;
    private final Clock clock;
    private final BalanceType currentBalanceType;
    private final BalanceType availableBalanceType;
    private final Map<String, AccountType> supportedAccountTypes;

    @Override
    public ProviderAccountDTO mapToAccount(UniCreditAccountDTO account, List<UniCreditTransactionsDTO> transactions, List<UniCreditBalanceDTO> balances) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban()))
                .bic(account.getBic())
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .lastRefreshed(ZonedDateTime.now(clock))
                .name(account.getName())
                .yoltAccountType(supportedAccountTypes.get(account.getCashAccountType()))
                .currentBalance(balanceMapper.getBalanceAmount(balances, currentBalanceType))
                .availableBalance(balanceMapper.getBalanceAmount(balances, availableBalanceType))
                .transactions(transactionMapper.mapTransactions(transactions))
                .extendedAccount(mapExtendedAccount(account, balances))
                .build();
    }

    private ExtendedAccountDTO mapExtendedAccount(final UniCreditAccountDTO account, final List<UniCreditBalanceDTO> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(balances.stream().map(balanceMapper::mapBalance).collect(Collectors.toList()))
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .cashAccountType(ExternalCashAccountType.fromCode(account.getCashAccountType()))
                .bic(account.getBic())
                .build();
    }

    @Override
    public boolean verifyAccountType(String cashAccountType) {
        return supportedAccountTypes.containsKey(cashAccountType);
    }
}
