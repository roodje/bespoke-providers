package com.yolt.providers.ing.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.ing.common.dto.Accounts.Account;
import com.yolt.providers.ing.common.dto.Balances;
import com.yolt.providers.ing.common.dto.CardTransactions;
import com.yolt.providers.ing.common.dto.CardTransactions.CardTransaction;
import com.yolt.providers.ing.common.dto.Transactions;
import com.yolt.providers.ing.common.dto.Transactions.TargetAccount;
import com.yolt.providers.ing.common.dto.Transactions.Transaction;
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
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class IngDataMapperServiceV6 {

    private static final String CARD_ACCOUNTS_ENDPOINT_PART = "card-accounts";
    protected static final String ING_TRANSACTION_DESCRIPTION_NOT_AVAILABLE = "N/A";
    protected static final Pattern LINE_BREAK_TAG_PATTERN = Pattern.compile("<br>");
    private final ObjectMapper objectMapper;
    private final ZoneId zoneId;

    public IngDataMapperServiceV6(ZoneId zoneId) {
        this.zoneId = zoneId;
        this.objectMapper = new ObjectMapper();
    }

    private static final DateTimeFormatter DATE_TIME_NANOSECONDS_OFFSET_FORMATTER =
            new DateTimeFormatterBuilder().parseCaseInsensitive().append(ISO_LOCAL_DATE_TIME)
                    .appendOffset("+HH:mm", "Z")
                    .toFormatter();

    public ProviderAccountDTO mapToProviderAccountDTO(final Account account, final Balances accountBalances, final Clock clock) {
        Optional<BigDecimal> interimBooked = getBalance(Balances.Type.INTERIMBOOKED, accountBalances);
        Optional<BigDecimal> expected = getBalance(Balances.Type.EXPECTED, accountBalances);
        Optional<BigDecimal> interimAvailable = getBalance(Balances.Type.INTERIMAVAILABLE, accountBalances);
        AccountType accountType = determineAccountType(account);
        ProviderCreditCardDTO creditCardData = AccountType.CREDIT_CARD.equals(accountType) ? new ProviderCreditCardDTO() : null;
        return ProviderAccountDTO.builder()
                .yoltAccountType(accountType)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(getFirstAvailableBalance(expected, interimBooked, interimAvailable))
                .currentBalance(getFirstAvailableBalance(expected, interimBooked, interimAvailable))
                .accountId(account.getId())
                .accountNumber(createProviderAccountNumberDTO(account))
                .accountMaskedIdentification(account.getMaskedPan())
                .name(account.getName())
                .currency(toCurrencyCode(account.getCurrency()))
                .extendedAccount(createExtendedAccountDTO(account, accountBalances))
                .transactions(new ArrayList<>())
                .creditCardData(creditCardData)
                .build();
    }

    private BigDecimal getFirstAvailableBalance(Optional<BigDecimal>... balances) {
        for (Optional<BigDecimal> balance : balances) {
            if (balance.isPresent()) {
                return balance.get();
            }
        }
        return null;
    }

    public List<ProviderTransactionDTO> mapToProviderTransactionDTO(final Transactions transactions) {
        List<ProviderTransactionDTO> transactionsConverted = new ArrayList<>();
        transactionsConverted.addAll(getConvertedTransactions(transactions.getBookedTransactions(), TransactionStatus.BOOKED));
        transactionsConverted.addAll(getConvertedTransactions(transactions.getPendingTransactions(), TransactionStatus.PENDING));
        return transactionsConverted;
    }

    public List<ProviderTransactionDTO> mapToProviderCardTransactionDTO(final CardTransactions transactions) {
        List<ProviderTransactionDTO> transactionsConverted = new ArrayList<>();
        transactionsConverted.addAll(getConvertedCardTransactions(transactions.getBookedTransactions(), TransactionStatus.BOOKED));
        transactionsConverted.addAll(getConvertedCardTransactions(transactions.getPendingTransactions(), TransactionStatus.PENDING));
        return transactionsConverted;
    }

    private ZonedDateTime parseZonedDateTime(final String dateToBeParsed) {
        return ZonedDateTime.parse(dateToBeParsed, DATE_TIME_NANOSECONDS_OFFSET_FORMATTER).withZoneSameInstant(zoneId);
    }

    ZonedDateTime parseLocalDateToZonedDateTime(final String dateToBeParsed) {
        return LocalDate.parse(dateToBeParsed, ISO_LOCAL_DATE).atStartOfDay(zoneId);
    }

    private ExtendedAccountDTO createExtendedAccountDTO(final Account account, final Balances accountBalances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getId())
                .currency(toCurrencyCode(account.getCurrency()))
                .name(account.getName())
                .product(account.getProduct())
                .balances(retrieveBalances(accountBalances))
                .build();
    }

    private AccountType determineAccountType(final Account account) {
        if (account.getTransactionLink() != null && account.getTransactionLink().contains(CARD_ACCOUNTS_ENDPOINT_PART)) {
            return AccountType.CREDIT_CARD;
        }
        return AccountType.CURRENT_ACCOUNT;
    }

    private ProviderAccountNumberDTO createProviderAccountNumberDTO(final Account account) {
        if (!StringUtils.isEmpty(account.getIban())) {
            ProviderAccountNumberDTO dto = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
            dto.setHolderName(account.getName());
            return dto;
        }
        return null;
    }

    private Optional<BigDecimal> getBalance(final Balances.Type type, final Balances accountBalances) {
        for (Balances.Balance balance : accountBalances.getData()) {
            if (type.getValue().equals(balance.getType())) {
                return Optional.of(balance.getAmount());
            }
        }
        return Optional.empty();
    }

    private List<BalanceDTO> retrieveBalances(final Balances accountBalances) {
        return accountBalances
                .getData()
                .stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(new BalanceAmountDTO(toCurrencyCode(balance.getCurrency()), balance.getAmount()))
                        .balanceType(BalanceType.fromName(balance.getType()))
                        .lastChangeDateTime(balance.getLastChangeDate() == null ? null : parseZonedDateTime(balance.getLastChangeDate()))
                        .referenceDate(balance.getReferenceDate() == null ? null : parseLocalDateToZonedDateTime(balance.getReferenceDate()))
                        .build())
                .collect(Collectors.toList());
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

    private List<ProviderTransactionDTO> getConvertedTransactions(final List<Transaction> transactions, final TransactionStatus status) {
        if (transactions == null) {
            return Collections.emptyList();
        }
        return transactions.stream()
                .map(transaction -> convertTransaction(transaction, status))
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO convertTransaction(final Transaction transaction, final TransactionStatus status) {
        String transactionDescription = retrieveSanitizedTransactionDescription(transaction);
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(transaction.getExecutionDateTime() == null ? null : parseZonedDateTime(transaction.getExecutionDateTime()))
                .amount(mapToAmount(transaction.getAmount()))
                .status(status)
                .type(mapToTransactionType(transaction.getAmount()))
                .description(transactionDescription)
                .category(YoltCategory.GENERAL)
                .merchant(transaction.getCreditorName())
                .extendedTransaction(createExtendedTransactionDTO(transaction, status, transactionDescription))
                .bankSpecific(retrieveBankSpecificInformation(transaction))
                .build();
    }

    private BigDecimal mapToAmount(BigDecimal amount) {
        if (ObjectUtils.isEmpty(amount)) {
            return null;
        }
        return amount.abs();
    }

    private ProviderTransactionType mapToTransactionType(BigDecimal amount) {
        if (ObjectUtils.isEmpty(amount)) {
            return null;
        }
        return amount.compareTo(BigDecimal.ZERO) > 0 ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT;
    }

    private List<ProviderTransactionDTO> getConvertedCardTransactions(final List<CardTransaction> transactions, final TransactionStatus status) {
        if (transactions == null) {
            return Collections.emptyList();
        }
        return transactions.stream().map(
                transaction -> ProviderTransactionDTO.builder()
                        .externalId(transaction.getCardTransactionId())
                        .dateTime(transaction.getTransactionDate() == null ? null : parseLocalDateToZonedDateTime(transaction.getTransactionDate()))
                        .amount(transaction.getAmount().abs())
                        .status(status)
                        .type(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0 ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT)
                        .description(retrieveTransactionDescription(transaction))
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(createExtendedTransactionDTO(transaction, status))
                        .build()
        ).collect(Collectors.toList());
    }

    private ExtendedTransactionDTO createExtendedTransactionDTO(final Transaction transaction, final TransactionStatus status, final String transactionDescription) {
        ZonedDateTime bookingDate = transaction.getBookingDate() == null ? null : parseLocalDateToZonedDateTime(transaction.getBookingDate());
        ZonedDateTime valueDate = transaction.getValueDate() == null ? null : parseLocalDateToZonedDateTime(transaction.getValueDate());
        return ExtendedTransactionDTO.builder()
                .status(status)
                .endToEndId(transaction.getEndToEndId())
                .bookingDate(bookingDate)
                .valueDate(valueDate)
                .transactionAmount(new BalanceAmountDTO(toCurrencyCode(transaction.getCurrency()), transaction.getAmount()))
                .creditorName(transaction.getCreditorName())
                .creditorAccount(retrieveAccountIban(transaction.getCreditorAccount()))
                .debtorName(transaction.getDebtorName())
                .debtorAccount(retrieveAccountIban(transaction.getDebtorAccount()))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .remittanceInformationStructured(retrieveRemittanceInformationStructured(transaction)).build();
    }

    private ExtendedTransactionDTO createExtendedTransactionDTO(final CardTransaction transaction, final TransactionStatus status) {
        ZonedDateTime bookingDate = transaction.getBookingDate() == null ? null : parseLocalDateToZonedDateTime(transaction.getBookingDate());
        ZonedDateTime valueDate = transaction.getTransactionDate() == null ? null : parseLocalDateToZonedDateTime(transaction.getTransactionDate());
        return ExtendedTransactionDTO.builder()
                .status(status)
                .bookingDate(bookingDate)
                .valueDate(valueDate)
                .transactionAmount(new BalanceAmountDTO(toCurrencyCode(transaction.getCurrency()), transaction.getAmount()))
                .creditorAccount(transaction.getAmount().signum() < 0 ? retrieveAccountMaskedPan(transaction.getMaskedPan()) : null)
                .debtorAccount(transaction.getAmount().signum() > 0 ? retrieveAccountMaskedPan(transaction.getMaskedPan()) : null)
                .remittanceInformationUnstructured(transaction.getDescription())
                .build();
    }

    private String retrieveRemittanceInformationStructured(final Transaction transaction) {
        return Optional.ofNullable(transaction.getRemittanceInformationStructured())
                .map(Transactions.RemittanceInformationStructured::getReference)
                .orElse(null);
    }

    private AccountReferenceDTO retrieveAccountIban(final TargetAccount account) {
        if (account != null) {

            if (StringUtils.isNotBlank(account.getIban()) || StringUtils.isBlank(account.getBban())) {
                return new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban());
            } else {
                return new AccountReferenceDTO(AccountReferenceType.BBAN, account.getBban());
            }
        }
        return null;
    }

    private AccountReferenceDTO retrieveAccountMaskedPan(final String maskedPan) {
        if (maskedPan != null) {
            return new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, maskedPan);
        }
        return null;
    }

    protected String retrieveSanitizedTransactionDescription(final Transaction transaction) {
        if (StringUtils.isNotBlank(transaction.getDescription())) {
            return LINE_BREAK_TAG_PATTERN.matcher(transaction.getDescription()).replaceAll("\n");
        }
        return ING_TRANSACTION_DESCRIPTION_NOT_AVAILABLE;
    }

    private String retrieveTransactionDescription(final CardTransaction cardTransaction) {
        if (StringUtils.isNotBlank(cardTransaction.getDescription())) {
            return cardTransaction.getDescription();
        }
        return ING_TRANSACTION_DESCRIPTION_NOT_AVAILABLE;
    }

    private Map<String, String> retrieveBankSpecificInformation(final Transaction transaction) {
        if (StringUtils.isNotEmpty(transaction.getTransactionType())) {
            HashMap<String, String> bankSpecificMap = new HashMap<>();
            bankSpecificMap.put("transactionType", transaction.getTransactionType());
            return bankSpecificMap;
        }
        return null;
    }
}
