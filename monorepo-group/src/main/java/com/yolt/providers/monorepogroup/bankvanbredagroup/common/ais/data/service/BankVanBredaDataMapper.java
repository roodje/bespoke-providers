package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.service;

import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto.Account;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto.Balance;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto.Transaction;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto.Transactions;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;

@RequiredArgsConstructor
public class BankVanBredaDataMapper {
    private final Clock clock;
    private final ZoneId zoneId = ZoneId.of("Europe/Brussels");

    public List<ProviderTransactionDTO> mapTransactions(Transactions transactions) {
        return Stream.concat(
                mapTransactions(transactions.getBookedTransactions(), TransactionStatus.BOOKED).stream(),
                mapTransactions(transactions.getPendingTransactions(), TransactionStatus.PENDING).stream()
        ).collect(Collectors.toList());
    }

    private List<ProviderTransactionDTO> mapTransactions(List<Transaction> transactions, TransactionStatus status) {
        if (CollectionUtils.isEmpty(transactions)) {
            return Collections.emptyList();
        }
        return transactions.stream().map(transaction -> {
            BigDecimal amount = transaction.getAmount();
            return ProviderTransactionDTO.builder()
                    .externalId(transaction.getTransactionId())
                    .dateTime(ZonedDateTime.of(transaction.getValueDate(), LocalTime.MIN, zoneId))
                    .amount(amount.abs())
                    .status(status)
                    .type(amount.compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT)
                    .description(transaction.getRemittanceInformationUnstructured())
                    .category(YoltCategory.GENERAL)
                    .extendedTransaction(mapExtendedTransaction(transaction, status))
                    .build();
        }).collect(Collectors.toList());
    }

    private ExtendedTransactionDTO mapExtendedTransaction(Transaction transaction, TransactionStatus status) {
        String creditorIban = transaction.getCreditorIban();
        return ExtendedTransactionDTO.builder()
                .status(status)
                .entryReference(transaction.getTransactionId())
                .bookingDate(ZonedDateTime.of(transaction.getBookingDate(), LocalTime.MIN, zoneId))
                .valueDate(ZonedDateTime.of(transaction.getValueDate(), LocalTime.MIN, zoneId))
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(transaction.getAmount())
                        .currency(toCurrencyCode(transaction.getCurrency()))
                        .build())
                .creditorName(transaction.getCreditorName())
                .creditorAccount(creditorIban != null ? new AccountReferenceDTO(AccountReferenceType.IBAN, creditorIban) : null)
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .transactionIdGenerated(false)
                .build();
    }

    private static CurrencyCode toCurrencyCode(final String currencyCode) {
        if (ObjectUtils.isEmpty(currencyCode)) {
            return null;
        }
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public ProviderAccountDTO mapAccountData(Account account, List<ProviderTransactionDTO> mappedTransactions) {
        BigDecimal interimAvailableBalance = getInterimAvailableBalance(account.getBalances());
        ProviderAccountNumberDTO accountNumber = getAccountNumber(account);
        return ProviderAccountDTO.builder()
                .yoltAccountType(mapToAccountType(account.getCashAccountType()))
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(interimAvailableBalance)
                .currentBalance(interimAvailableBalance)
                .accountId(account.getResourceId())
                .accountNumber(accountNumber)
                .bic(account.getBic())
                .name(account.getName())
                .currency(toCurrencyCode(account.getCurrency()))
                .transactions(mappedTransactions)
                .extendedAccount(mapExtendedAccountData(account))
                .build();
    }

    private AccountType mapToAccountType(String type) {
        if ("CACC".equals(type)) {
            return CURRENT_ACCOUNT;
        }
        return null;
    }

    private ProviderAccountNumberDTO getAccountNumber(Account account) {
        if (account.getIban() == null) {
            return null;
        }
        ProviderAccountNumberDTO accountNumber = new ProviderAccountNumberDTO(IBAN, account.getIban());
        accountNumber.setHolderName(account.getOwnerName());
        return accountNumber;
    }

    private ExtendedAccountDTO mapExtendedAccountData(Account account) {
        CurrencyCode currency = toCurrencyCode(account.getCurrency());
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(getAccountReference(account))
                .currency(currency)
                .name(account.getName())
                .product(account.getProduct())
                .cashAccountType(ExternalCashAccountType.fromCode(account.getCashAccountType()))
                .bic(account.getBic())
                .balances(account.getBalances()
                        .stream()
                        .map(balance -> BalanceDTO.builder()
                                .balanceAmount(BalanceAmountDTO.builder()
                                        .amount(balance.getAmount())
                                        .currency(currency)
                                        .build())
                                .balanceType(BalanceType.fromName(balance.getBalanceType()))
                                .lastChangeDateTime(ZonedDateTime.of(balance.getLastChangeDateTime(), zoneId))
                                .referenceDate(ZonedDateTime.of(balance.getReferenceDate(), LocalTime.MIN, zoneId))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private List<AccountReferenceDTO> getAccountReference(Account account) {
        if (account.getIban() == null) {
            return Collections.emptyList();
        }
        AccountReferenceDTO accountNumber = new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban());
        return List.of(accountNumber);
    }

    private BigDecimal getInterimAvailableBalance(List<Balance> accountBalances) {
        BigDecimal balanceValue = null;
        LocalDate balanceReferenceDate = null;
        for (Balance balance : accountBalances) {
            if (BalanceType.INTERIM_AVAILABLE.getName().equals(balance.getBalanceType()) && (balanceValue == null || balance.getReferenceDate().isAfter(balanceReferenceDate))) {
                balanceValue = balance.getAmount();
                balanceReferenceDate = balance.getReferenceDate();
            }
        }
        return balanceValue;
    }
}
