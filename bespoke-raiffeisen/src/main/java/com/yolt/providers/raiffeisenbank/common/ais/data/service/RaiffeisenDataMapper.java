package com.yolt.providers.raiffeisenbank.common.ais.data.service;

import com.yolt.providers.raiffeisenbank.common.ais.data.dto.Account;
import com.yolt.providers.raiffeisenbank.common.ais.data.dto.Balance;
import com.yolt.providers.raiffeisenbank.common.ais.data.dto.Transaction;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExchangeRateDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.ing.lovebird.extendeddata.account.ExternalCashAccountType.CURRENT;
import static nl.ing.lovebird.extendeddata.account.ExternalCashAccountType.SAVINGS;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.AccountType.SAVINGS_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;


@AllArgsConstructor
public class RaiffeisenDataMapper {
    private final ZoneId zoneId;
    private final Clock clock;

    public List<ProviderTransactionDTO> mapTransactions(List<Transaction> transactions, TransactionStatus status) {
        if (CollectionUtils.isEmpty(transactions)) {
            return Collections.emptyList();
        }
        return transactions.stream().map(transaction -> {
            BigDecimal amount = transaction.getAmount();
            return ProviderTransactionDTO.builder()
                    .externalId(transaction.getTransactionId())
                    .dateTime(toDate(transaction.getValueDate()))
                    .amount(amount)
                    .status(status)
                    .type(amount.compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT)
                    .description(transaction.getRemittanceInformationUnstructured())
                    .category(YoltCategory.GENERAL)
                    .merchant(transaction.getCreditorName())
                    .extendedTransaction(mapExtendedTransaction(transaction, status))
                    .build();
        }).collect(Collectors.toList());
    }

    private ExtendedTransactionDTO mapExtendedTransaction(Transaction transaction, TransactionStatus status) {
        return ExtendedTransactionDTO.builder()
                .status(status)
                .entryReference(transaction.getTransactionId())
                .endToEndId(transaction.getEndToEndId())
                .mandateId(transaction.getMandateId())
                .bookingDate(toDate(transaction.getBookingDate()))
                .valueDate(toDate(transaction.getValueDate()))
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(transaction.getAmount())
                        .currency(toCurrencyCode(transaction.getCurrency()))
                        .build())
                .exchangeRate(transaction.getExchangeRate()
                        .stream()
                        .map(exchangeRate -> ExchangeRateDTO.builder()
                                .currencyFrom(toCurrencyCode(exchangeRate.getSourceCurrency()))
                                .rateFrom(exchangeRate.getExchangeRate())
                                .currencyTo(toCurrencyCode(exchangeRate.getTargetCurrency()))
                                .rateDate(toDate(exchangeRate.getQuotationDate()))
                                .rateContract(exchangeRate.getContractIdentification())
                                .build())
                        .collect(Collectors.toList()))
                .creditorName(transaction.getCreditorName())
                .creditorAccount(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value(transaction.getCreditorIban())
                        .build())
                .ultimateCreditor(transaction.getUltimateCreditor())
                .debtorName(transaction.getDebtorName())
                .debtorAccount(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value(transaction.getDebtorIban())
                        .build())
                .ultimateDebtor(transaction.getUltimateDebtor())
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .remittanceInformationStructured(transaction.getRemittanceInformationStructured())
                .purposeCode(transaction.getPurposeCode())
                .bankTransactionCode(transaction.getBankTransactionCode())
                .transactionIdGenerated(false)
                .build();
    }

    private ZonedDateTime toDate(LocalDate transactionDate) {
        if (transactionDate == null) {
            return null;
        }
        return transactionDate.atStartOfDay(zoneId);
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

    public ProviderAccountDTO mapAccountData(Account account, List<ProviderTransactionDTO> transactions) {
        Optional<BigDecimal> interimAvailableBalance = getBalance(account.getBalances(), BalanceType.INTERIM_AVAILABLE);
        Optional<BigDecimal> expected = getBalance(account.getBalances(), BalanceType.EXPECTED);
        ProviderAccountNumberDTO accountNumber = new ProviderAccountNumberDTO(IBAN, account.getIban());
        accountNumber.setHolderName(account.getOwnerName());
        return ProviderAccountDTO.builder()
                .yoltAccountType(mapAccountType(account.getCashAccountType()))
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(getFirstAvailableBalance(interimAvailableBalance, expected))
                .currentBalance(getFirstAvailableBalance(expected, interimAvailableBalance))
                .accountId(account.getResourceId())
                .accountNumber(accountNumber)
                .bic(account.getBic())
                .name(account.getAccountName())
                .currency(toCurrencyCode(account.getCurrency()))
                .transactions(transactions)
                .extendedAccount(mapExtendedAccountData(account))
                .build();
    }

    private ExtendedAccountDTO mapExtendedAccountData(Account account) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(List.of(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value(account.getIban())
                        .build()))
                .currency(toCurrencyCode(account.getCurrency()))
                .name(account.getAccountName())
                .product(account.getProduct())
                .cashAccountType(mapExternalCashAccountType(account.getCashAccountType()))
                .bic(account.getBic())
                .balances(account.getBalances()
                        .stream()
                        .map(balance -> BalanceDTO.builder()
                                .balanceAmount(BalanceAmountDTO.builder()
                                        .amount(balance.getAmount())
                                        .currency(toCurrencyCode(balance.getCurrency()))
                                        .build())
                                .balanceType(mapToBalanceType(balance.getType()))
                                .referenceDate(toDate(balance.getReferenceDate()))
                                .build())
                        .collect(Collectors.toList()))
                .build();

    }

    private BalanceType mapToBalanceType(String type) {
        return switch (type) {
            case "interimAvailable" -> BalanceType.INTERIM_AVAILABLE;
            case "expected" -> BalanceType.EXPECTED;
            default -> null;
        };
    }

    private AccountType mapAccountType(String accountType) {
        return switch (accountType) {
            case "CACC" -> CURRENT_ACCOUNT;
            case "SVGS" -> SAVINGS_ACCOUNT;
            default -> null;
        };
    }

    private ExternalCashAccountType mapExternalCashAccountType(String accountType) {
        return switch (accountType) {
            case "CACC" -> CURRENT;
            case "SVGS" -> SAVINGS;
            default -> null;
        };
    }

    private BigDecimal getFirstAvailableBalance(Optional<BigDecimal>... balances) {
        for (Optional<BigDecimal> balance : balances) {
            if (balance.isPresent()) {
                return balance.get();
            }
        }
        return null;
    }

    private Optional<BigDecimal> getBalance(List<Balance> accountBalances, BalanceType type) {
        BigDecimal balanceValue = null;
        LocalDate balanceReferenceDate = null;
        for (Balance balance : accountBalances) {
            if (type.getName().equals(balance.getType()) && (balanceValue == null || balance.getReferenceDate().isAfter(balanceReferenceDate))) {
                balanceValue = balance.getAmount();
                balanceReferenceDate = balance.getReferenceDate();
            }
        }
        return Optional.of(balanceValue);
    }
}
