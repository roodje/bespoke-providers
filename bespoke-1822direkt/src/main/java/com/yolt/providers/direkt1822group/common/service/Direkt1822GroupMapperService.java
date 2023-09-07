package com.yolt.providers.direkt1822group.common.service;

import com.yolt.providers.direkt1822group.common.dto.*;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.AccountType.SAVINGS_ACCOUNT;

@RequiredArgsConstructor
public class Direkt1822GroupMapperService {

    private final Clock clock;

    private static final DateTimeFormatter DATE_TIME_NANOSECONDS_OFFSET_FORMATTER =
            new DateTimeFormatterBuilder().parseCaseInsensitive().append(ISO_LOCAL_DATE_TIME)
                    .appendOffset("+HH:mm", "Z")
                    .toFormatter();
    private static final String DIREKT1822_ACCOUNT_NAME = "1822 Direkt Account";
    private static final ZoneId BERLIN_TIME_ZONE = ZoneId.of("Europe/Berlin");

    ProviderAccountDTO createProviderAccountDTO(final Account account) {
        return ProviderAccountDTO.builder()
                .yoltAccountType(toAccountType(account.getCashAccountType()))
                .accountId(account.getResourceId())
                .accountNumber(createProviderAccountNumberDTO(account))
                .name(DIREKT1822_ACCOUNT_NAME)
                .currency(toCurrencyCode(account.getCurrency()))
                .extendedAccount(createExtendedAccountDTO(account))
                .transactions(new ArrayList<>())
                .build();
    }

    ProviderAccountDTO updateAccountDTOWithBalances(ProviderAccountDTO account, BalancesResponse accountBalances) {
        BigDecimal interimBooked = getBalance(Balance.Type.CLOSING_BOOKED, accountBalances);
        BigDecimal interimAvailable = getBalance(Balance.Type.AUTHORISED, accountBalances);
        return ProviderAccountDTO.builder()
                .yoltAccountType(account.getYoltAccountType())
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(getFirstNotEmptyBalance(interimAvailable, interimBooked))
                .currentBalance(getFirstNotEmptyBalance(interimBooked, interimAvailable))
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .name(account.getName())
                .currency(account.getCurrency())
                .extendedAccount(account.getExtendedAccount()
                        .toBuilder()
                        .balances(retrieveBalances(accountBalances))
                        .build())
                .transactions(new ArrayList<>())
                .build();
    }

    private ProviderAccountNumberDTO createProviderAccountNumberDTO(final Account account) {
        if (StringUtils.isNotEmpty(account.getIban())) {
            ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
            providerAccountNumberDTO.setHolderName(account.getOwnerName());
            return providerAccountNumberDTO;
        }
        return null;
    }

    private static CurrencyCode toCurrencyCode(final String currencyCode) {
        if (StringUtils.isEmpty(currencyCode)) {
            return null;
        }
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    private static AccountType toAccountType(String cashAccountType) {
        if (StringUtils.isEmpty(cashAccountType)) {
            return null;
        }
        return switch (cashAccountType) {
            case "CACC" -> CURRENT_ACCOUNT;
            case "SVGS" -> SAVINGS_ACCOUNT;
            default -> null;
        };
    }

    private ExtendedAccountDTO createExtendedAccountDTO(final Account account) {
        ExtendedAccountDTO.ExtendedAccountDTOBuilder builder = ExtendedAccountDTO.builder();
        List<AccountReferenceDTO> accountNumbers = new ArrayList<>();
        if (account.getIban() != null) {
            accountNumbers.add(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban()));
        }
        return builder.resourceId(account.getResourceId())
                .currency(toCurrencyCode(account.getCurrency()))
                .name(account.getName())
                .product(account.getProduct())
                .accountReferences(accountNumbers.isEmpty() ? null : accountNumbers)
                .build();
    }

    private BigDecimal getBalance(final Balance.Type type, final BalancesResponse accountBalances) {
        return accountBalances.getBalances()
                .stream()
                .filter(balance -> type.getName().equals(balance.getBalanceType()))
                .map(balance -> balance.getBalanceAmount().getAmount())
                .findFirst()
                .orElse(null);
    }

    private BigDecimal getFirstNotEmptyBalance(BigDecimal... balances) {
        return Arrays.stream(balances)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<BalanceDTO> retrieveBalances(final BalancesResponse accountBalances) {
        return accountBalances
                .getBalances()
                .stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(new BalanceAmountDTO(toCurrencyCode(balance.getBalanceAmount().getCurrency()), balance.getBalanceAmount().getAmount()))
                        .balanceType(BalanceType.fromName(balance.getBalanceType()))
                        .lastChangeDateTime(balance.getLastChangeDateTime() == null ? null : parseZonedDateTime(balance.getLastChangeDateTime()))
                        .build())
                .collect(Collectors.toList());
    }

    private ZonedDateTime parseZonedDateTime(final String dateToBeParsed) {
        return ZonedDateTime.parse(dateToBeParsed, DATE_TIME_NANOSECONDS_OFFSET_FORMATTER).withZoneSameInstant(BERLIN_TIME_ZONE);
    }

    private ZonedDateTime parseLocalDateToZonedDateTime(final String dateToBeParsed) {
        return LocalDate.parse(dateToBeParsed, ISO_LOCAL_DATE).atStartOfDay(BERLIN_TIME_ZONE);
    }

    List<ProviderTransactionDTO> mapToProviderTransactionDTO(Transactions transactions) {
        List<ProviderTransactionDTO> transactionDTOS = new ArrayList<>();

        if (transactions.getBooked() != null) {
            transactionDTOS.addAll(
                    transactions.getBooked().stream()
                            .map(transaction -> convertTransaction(transaction, BOOKED))
                            .collect(Collectors.toList()));
        }

        if (transactions.getPending() != null) {
            transactionDTOS.addAll(
                    transactions.getPending().stream()
                            .map(transaction -> convertTransaction(transaction, PENDING))
                            .collect(Collectors.toList()));
        }

        return transactionDTOS;
    }

    private ProviderTransactionDTO convertTransaction(final Transaction transaction,
                                                      final TransactionStatus status) {
        BigDecimal amount = transaction.getTransactionAmount().getAmount();

        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(getDateTime(transaction))
                .type(amount.compareTo(BigDecimal.ZERO) > 0
                        ? ProviderTransactionType.CREDIT
                        : ProviderTransactionType.DEBIT)
                .category(YoltCategory.GENERAL)
                .amount(amount.abs())
                .description(transaction.getRemittanceInformationStructured())
                .status(status)
                .extendedTransaction(mapToExtendedTransaction(transaction, status))
                .build();
    }

    private ZonedDateTime getDateTime(Transaction transaction) {
        return LocalDate.parse(transaction.getBookingDate() != null ? transaction.getBookingDate() : transaction.getValueDate())
                .atStartOfDay(BERLIN_TIME_ZONE);
    }

    private static BalanceAmountDTO mapToTransactionAmount(final Transaction transaction) {
        return BalanceAmountDTO.builder()
                .currency(toCurrencyCode(transaction.getTransactionAmount().getCurrency()))
                .amount(transaction.getTransactionAmount().getAmount())
                .build();
    }

    private ExtendedTransactionDTO mapToExtendedTransaction(final Transaction transaction,
                                                            final TransactionStatus status) {

        return ExtendedTransactionDTO.builder()
                .bookingDate(transaction.getBookingDate() != null ? parseLocalDateToZonedDateTime(transaction.getBookingDate()) : null)
                .creditorAccount(mapAccountReferenceToExtendedAccountReferences(transaction.getCreditorAccount()))
                .creditorName(transaction.getCreditorName())
                .debtorAccount(mapAccountReferenceToExtendedAccountReferences(transaction.getDebtorAccount()))
                .debtorName(transaction.getDebtorName())
                .entryReference(transaction.getEntryReference())
                .endToEndId(transaction.getEndToEndId())
                .mandateId(transaction.getMandateId())
                .creditorId(transaction.getCreditorId())
                .remittanceInformationStructured(transaction.getRemittanceInformationStructured())
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .status(status)
                .transactionAmount(mapToTransactionAmount(transaction))
                .transactionIdGenerated(true)
                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .valueDate(transaction.getValueDate() != null ? parseLocalDateToZonedDateTime(transaction.getValueDate()) : null)
                .build();
    }

    private static AccountReferenceDTO mapAccountReferenceToExtendedAccountReferences(final AccountReference sourceAccount) {
        if (sourceAccount != null && StringUtils.isNotBlank(sourceAccount.getIban())) {
            return new AccountReferenceDTO(AccountReferenceType.IBAN, sourceAccount.getIban());
        }
        return null;
    }
}
