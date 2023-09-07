package com.yolt.providers.knabgroup.common.data;

import com.yolt.providers.knabgroup.common.dto.external.Accounts.Account;
import com.yolt.providers.knabgroup.common.dto.external.Balances;
import com.yolt.providers.knabgroup.common.dto.external.Transactions;
import com.yolt.providers.knabgroup.common.dto.external.Transactions.ExchangeRate;
import com.yolt.providers.knabgroup.common.dto.external.Transactions.TargetAccount;
import com.yolt.providers.knabgroup.common.dto.external.Transactions.Transaction;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExchangeRateDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.AccountType.SAVINGS_ACCOUNT;

@RequiredArgsConstructor
public class KnabGroupMapperServiceV2 {

    private final ZoneId zoneId;

    private static final DateTimeFormatter DATE_TIME_NANOSECONDS_OFFSET_FORMATTER =
            new DateTimeFormatterBuilder().parseCaseInsensitive().append(ISO_LOCAL_DATE_TIME)
                    .appendOffset("+HH:mm", "Z")
                    .toFormatter();

    ProviderAccountDTO updateProviderAccountDTO(final ProviderAccountDTO account, final Balances accountBalances, final Clock clock) {
        Optional<BigDecimal> interimBooked = getBalance(Balances.Type.INTERIMBOOKED, accountBalances);
        Optional<BigDecimal> interimAvailable = getBalance(Balances.Type.INTERIMAVAILABLE, accountBalances);
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

    ProviderAccountDTO createProviderAccountDTO(final Account account) {
        return ProviderAccountDTO.builder()
                .yoltAccountType(mapToYoltAccountType(account.getAccountType()))
                .accountId(account.getId())
                .accountNumber(createProviderAccountNumberDTO(account))
                .name(account.getProduct())
                .currency(toCurrencyCode(account.getCurrency()))
                .extendedAccount(createExtendedAccountDTO(account))
                .transactions(new ArrayList<>())
                .build();
    }

    List<ProviderTransactionDTO> mapToProviderTransactionDTO(final Transactions transactions) {
        if (transactions == null) {
            return Collections.emptyList();
        }
        return transactions.getTransactions().stream()
                .map(this::convertTransaction)
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO convertTransaction(final Transaction transaction) {
        String transactionDate = transaction.getTransactionDate();
        String transactionDescription = getTransactionDescription(transaction);
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(transactionDate == null ? null : parseLocalDateToZonedDateTime(transactionDate))
                .amount(mapToAmount(transaction.getAmount()))
                .status(BOOKED)
                .type(mapToTransactionType(transaction.getAmount()))
                .description(transactionDescription)
                .category(YoltCategory.GENERAL)
                .merchant(transaction.getCreditorName())
                .extendedTransaction(createExtendedTransactionDTO(transaction))
                .bankSpecific(transaction.getDayStartBalance() != null ? getBankSpecificMap(transaction.getDayStartBalance()) : null)
                .build();
    }

    private Map<String, String> getBankSpecificMap(Transactions.DayStartBalance dayStartBalance) {
        return TransactionBankSpecific.builder()
                .dayStartBalanceAmount(dayStartBalance.getAmount())
                .dayStartBalanceCurrency(dayStartBalance.getCurrency())
                .build().toMap();
    }

    private String getTransactionDescription(final Transaction transaction) {
        String remittanceInformation = transaction.getRemittanceInformationUnstructured();
        String proprietaryBankTransactionCode = transaction.getProprietaryBankTransactionCode();
        return (remittanceInformation != null ? remittanceInformation : "") +
                (remittanceInformation != null && proprietaryBankTransactionCode != null ? " - " : "") +
                (proprietaryBankTransactionCode != null ? proprietaryBankTransactionCode : "");
    }

    private ExtendedTransactionDTO createExtendedTransactionDTO(final Transaction transaction) {
        ZonedDateTime bookingDate = transaction.getBookingDate() == null ? null : parseLocalDateToZonedDateTime(transaction.getBookingDate());
        ZonedDateTime valueDate = transaction.getValueDate() == null ? null : parseLocalDateToZonedDateTime(transaction.getValueDate());
        List<ExchangeRateDTO> exchangeRate = transaction.getExchangeRate() == null ? null : mapExchangeRate(transaction.getExchangeRate());
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .endToEndId(transaction.getEndToEndId())
                .bookingDate(bookingDate)
                .valueDate(valueDate)
                .transactionAmount(new BalanceAmountDTO(toCurrencyCode(transaction.getCurrency()), transaction.getAmount()))
                .creditorName(transaction.getCreditorName())
                .creditorAccount(retrieveAccountNumber(transaction.getCreditorAccount()))
                .debtorName(transaction.getDebtorName())
                .debtorAccount(retrieveAccountNumber(transaction.getDebtorAccount()))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .remittanceInformationStructured(transaction.getRemittanceInformationStructured())
                .exchangeRate(exchangeRate)
                .entryReference(transaction.getEntryReference())
                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .creditorId(transaction.getCreditorId())
                .mandateId(transaction.getMandateId())
                .build();
    }

    private List<ExchangeRateDTO> mapExchangeRate(final List<ExchangeRate> exchangeRates) {
        return exchangeRates.stream().map(exchangeRate -> ExchangeRateDTO.builder()
                .currencyFrom(toCurrencyCode(exchangeRate.getCurrencyFrom()))
                .currencyTo(toCurrencyCode(exchangeRate.getCurrencyTo()))
                .rateContract(exchangeRate.getContract())
                .rateDate(exchangeRate.getRateDate() == null ? null : parseLocalDateToZonedDateTime(exchangeRate.getRateDate()))
                .rateTo(exchangeRate.getRate())
                .build()).collect(Collectors.toList());
    }

    private AccountReferenceDTO retrieveAccountNumber(final TargetAccount account) {
        if (account == null) {
            return null;
        }
        if (account.getIban() != null) {
            return new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban());
        }
        if (account.getBban() != null) {
            return new AccountReferenceDTO(AccountReferenceType.BBAN, account.getBban());
        }
        return null;
    }

    private BigDecimal mapToAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.abs();
    }

    private ProviderTransactionType mapToTransactionType(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.compareTo(BigDecimal.ZERO) > 0 ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT;
    }


    private BigDecimal getFirstNotEmptyBalance(Optional<BigDecimal>... balances) {
        return Arrays.stream(balances)
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get)
                .orElse(null);
    }

    private Optional<BigDecimal> getBalance(final Balances.Type type, final Balances accountBalances) {
        return accountBalances.getData()
                .stream()
                .filter(balance -> type.getValue().equals(balance.getType()))
                .map(Balances.Balance::getAmount)
                .findFirst();
    }

    private ProviderAccountNumberDTO createProviderAccountNumberDTO(final Account account) {
        if (StringUtils.isNotEmpty(account.getIban())) {
            ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
            providerAccountNumberDTO.setHolderName(account.getName());
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

    private ExtendedAccountDTO createExtendedAccountDTO(final Account account) {
        ExtendedAccountDTO.ExtendedAccountDTOBuilder builder = ExtendedAccountDTO.builder();
        setAccountNumbers(builder, account.getIban(), account.getBban());
        return builder.resourceId(account.getId())
                .currency(toCurrencyCode(account.getCurrency()))
                .name(account.getName())
                .product(account.getProduct())
                .build();
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

    private List<BalanceDTO> retrieveBalances(final Balances accountBalances) {
        return accountBalances
                .getData()
                .stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(new BalanceAmountDTO(toCurrencyCode(balance.getCurrency()), balance.getAmount()))
                        .balanceType(BalanceType.fromName(balance.getType()))
                        .lastChangeDateTime(balance.getLastChangeDate() == null ? null : parseZonedDateTime(balance.getLastChangeDate()))
                        .lastCommittedTransaction(balance.getLastCommittedTransaction())
                        .build())
                .collect(Collectors.toList());
    }

    private ZonedDateTime parseZonedDateTime(final String dateToBeParsed) {
        return ZonedDateTime.parse(dateToBeParsed, DATE_TIME_NANOSECONDS_OFFSET_FORMATTER).withZoneSameInstant(zoneId);
    }

    private ZonedDateTime parseLocalDateToZonedDateTime(final String dateToBeParsed) {
        return LocalDate.parse(dateToBeParsed, ISO_LOCAL_DATE).atStartOfDay(zoneId);
    }

    private AccountType mapToYoltAccountType(String accountType) {
        if ("SVGS".equals(accountType)) {
            return SAVINGS_ACCOUNT;
        }
        else {
            return CURRENT_ACCOUNT;
        }
    }

}
