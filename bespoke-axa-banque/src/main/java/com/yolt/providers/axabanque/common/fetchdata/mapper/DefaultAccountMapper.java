package com.yolt.providers.axabanque.common.fetchdata.mapper;

import com.yolt.providers.axabanque.common.model.external.Account;
import com.yolt.providers.axabanque.common.model.external.Balance;
import com.yolt.providers.axabanque.common.model.external.Transactions;
import com.yolt.providers.common.mapper.currency.CurrencyCodeMapper;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderCreditCardDTO;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static nl.ing.lovebird.extendeddata.account.BalanceType.*;

@AllArgsConstructor
public class DefaultAccountMapper implements AccountMapper {

    private final ZoneId zoneId;
    private final TransactionMapper transactionMapper;
    private final AccountTypeMapper accountTypeMapper;
    private final CurrencyCodeMapper currencyCodeMapper;
    private final Clock clock;

    @Override
    public ProviderAccountDTO map(Account account, List<Balance> balances, List<Transactions> transactions) {

        final BigDecimal availableBalance = findBalance(balances, getPreferredBalanceTypesForAvailableBalance());
        final BigDecimal currentBalance = findBalance(balances, getPreferredBalanceTypesForCurrentBalance());
        final AccountType accountType = accountTypeMapper.mapToAccountType(account);

        ProviderAccountDTO.ProviderAccountDTOBuilder providerAccountDTOBuilder = ProviderAccountDTO.builder()
                .yoltAccountType(accountType)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(availableBalance)
                .currentBalance(currentBalance)
                .accountId(account.getResourceId())
                .accountNumber(createProviderAccountNumberDTO(account))
                .name(account.getName())
                .currency(currencyCodeMapper.mapToCurrencyCode(account.getCurrency()))
                .extendedAccount(mapToExtendedAccount(account, balances))
                .transactions(transactionMapper.mapToTransactions(transactions))
                .bic(account.getBic());
        if (accountType.equals(AccountType.CREDIT_CARD)) {
            providerAccountDTOBuilder.creditCardData(ProviderCreditCardDTO.builder()
                    .availableCreditAmount(availableBalance)
                    .build());
        }

        return providerAccountDTOBuilder.build();
    }

    private List<BalanceType> getPreferredBalanceTypesForCurrentBalance() {
        return Arrays.asList(CLOSING_BOOKED, INTERIM_BOOKED, INTERIM_AVAILABLE, OPENING_BOOKED, EXPECTED);
    }

    private List<BalanceType> getPreferredBalanceTypesForAvailableBalance() {
        return Arrays.asList(INTERIM_AVAILABLE, INTERIM_BOOKED, OPENING_BOOKED, CLOSING_BOOKED, EXPECTED);
    }

    private BigDecimal findBalanceForPreferredBalance(final List<Balance> balances, final String preferredBalance) {
        return balances.stream()
                .filter(balance -> preferredBalance.equalsIgnoreCase(balance.getType()))
                .findFirst()
                .map(balance -> BigDecimal.valueOf(balance.getAmount()))
                .orElse(null);
    }

    private BigDecimal findBalance(final List<Balance> balances,
                                   final List<BalanceType> preferredBalances) {
        for (BalanceType preferredBalance : preferredBalances) {
            BigDecimal balanceAmount = findBalanceForPreferredBalance(balances, preferredBalance.name());
            if (balanceAmount != null) {
                return balanceAmount;
            }
        }
        Balance firstBalance = balances.stream()
                .findFirst()
                .orElseGet(null);

        return firstBalance == null ? null : BigDecimal.valueOf(firstBalance.getAmount());
    }

    public ExtendedAccountDTO mapToExtendedAccount(final Account account, final List<Balance> balances) {
        ExtendedAccountDTO.ExtendedAccountDTOBuilder builder = ExtendedAccountDTO.builder();
        setAccountNumbers(builder, account.getIban(), account.getBban());
        return builder.resourceId(account.getResourceId())
                .currency(currencyCodeMapper.mapToCurrencyCode(account.getCurrency()))
                .name(account.getName())
                .cashAccountType(ExternalCashAccountType.fromName(account.getCashAccountType()))
                .bic(account.getBic())
                .balances(mapBalances(balances))
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .build();
    }

    private List<BalanceDTO> mapBalances(List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(new BalanceAmountDTO(currencyCodeMapper.mapToCurrencyCode(balance.getCurrency()), BigDecimal.valueOf(balance.getAmount())))
                        .balanceType(BalanceType.fromName(balance.getType()))
                        .lastChangeDateTime(balance.getLastChangeDate() == null ? null : toNullableZonedDateTime(balance.getLastChangeDate()))
                        .lastCommittedTransaction(balance.getLastCommittedTransaction())
                        .build())
                .collect(Collectors.toList());
    }

    private ZonedDateTime toNullableZonedDateTime(String dateTime) {
        if (StringUtils.isNotEmpty(dateTime)) {
            return ZonedDateTime.of(LocalDateTime.parse(dateTime), zoneId);
        }
        return null;
    }

    private void setAccountNumbers(ExtendedAccountDTO.ExtendedAccountDTOBuilder builder, final String iban, final String bban) {
        List<AccountReferenceDTO> accountNumbers = new ArrayList<>();
        if (iban != null) {
            accountNumbers.add(new AccountReferenceDTO(AccountReferenceType.IBAN, iban));
        }
        if (bban != null) {
            accountNumbers.add(new AccountReferenceDTO(AccountReferenceType.BBAN, bban));
        }
        builder.accountReferences(accountNumbers.isEmpty() ? null : accountNumbers);
    }

    private ProviderAccountNumberDTO createProviderAccountNumberDTO(final Account account) {
        if (StringUtils.isEmpty(account.getIban())) {
            return null;
        }
        return new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
    }

}
