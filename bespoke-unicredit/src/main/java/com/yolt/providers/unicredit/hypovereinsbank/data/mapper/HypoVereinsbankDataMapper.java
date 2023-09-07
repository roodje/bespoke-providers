package com.yolt.providers.unicredit.hypovereinsbank.data.mapper;

import com.yolt.providers.unicredit.common.data.mapper.BalanceMapper;
import com.yolt.providers.unicredit.common.data.mapper.CurrencyCodeMapper;
import com.yolt.providers.unicredit.common.data.mapper.TransactionMapper;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditDataMapper;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class HypoVereinsbankDataMapper implements UniCreditDataMapper {

    public static final String DEFAULT_CURRENT_ACCOUNT_NAME = "HypoVereinsbank current account";
    private final CurrencyCodeMapper currencyCodeMapper;
    private final BalanceMapper balanceMapper;
    private final TransactionMapper transactionMapper;
    private final Clock clock;
    private final BalanceType currentBalanceType;
    private final BalanceType availableBalanceType;

    @Override
    public ProviderAccountDTO mapToAccount(UniCreditAccountDTO account, List<UniCreditTransactionsDTO> transactions, List<UniCreditBalanceDTO> balances) {
        return ProviderAccountDTO.builder()
                .name(DEFAULT_CURRENT_ACCOUNT_NAME)
                .accountId(account.getResourceId())
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban()))
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .lastRefreshed(ZonedDateTime.now(clock))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .currentBalance(balanceMapper.getBalanceAmount(balances, currentBalanceType))
                .availableBalance(balanceMapper.getBalanceAmount(balances, availableBalanceType))
                .transactions(transactionMapper.mapTransactions(transactions))
                .extendedAccount(mapExtendedAccount(account, balances))
                .build();
    }

    private ExtendedAccountDTO mapExtendedAccount(final UniCreditAccountDTO account, final List<UniCreditBalanceDTO> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .product(account.getProduct())
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(balances.stream().map(balanceMapper::mapBalance).collect(Collectors.toList()))
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .build();
    }

    @Override
    public boolean verifyAccountType(String cashAccountType) {
        return true;
    }
}
