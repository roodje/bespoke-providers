package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.unicredit.common.dto.UniCreditAccountDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditBalanceDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditTransactionDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditTransactionsDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExchangeRateDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.slf4j.Marker;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static net.logstash.logback.marker.Markers.append;
import static nl.ing.lovebird.extendeddata.account.BalanceType.EXPECTED;
import static nl.ing.lovebird.extendeddata.account.BalanceType.INTERIM_AVAILABLE;

@Slf4j
@AllArgsConstructor
public class UniCreditDataMapperV2 implements UniCreditDataMapper {
    private final CurrencyCodeMapper currencyCodeMapper;
    private static final BalanceType CURRENT_BALANCE_TYPE = EXPECTED;
    private static final BalanceType AVAILABLE_BALANCE_TYPE = INTERIM_AVAILABLE;

    private static final Map<String, AccountType> SUPPORTED_ACCOUNT_TYPES = new HashMap<>();
    private static final Marker RDD_MARKER = append("raw-data", "true");
    private final Clock clock;

    static {
        SUPPORTED_ACCOUNT_TYPES.put(ExternalCashAccountType.CURRENT.getCode(), AccountType.CURRENT_ACCOUNT);
        SUPPORTED_ACCOUNT_TYPES.put(ExternalCashAccountType.CASH_INCOME.getCode(), AccountType.CREDIT_CARD);
    }

    @Override
    public ProviderAccountDTO mapToAccount(final UniCreditAccountDTO account,
                                           final List<UniCreditTransactionsDTO> transactions,
                                           final List<UniCreditBalanceDTO> balances) {
        Optional<BigDecimal> availableBalance = retrieveBalance(AVAILABLE_BALANCE_TYPE, balances);
        Optional<BigDecimal> currentBalance = retrieveBalance(CURRENT_BALANCE_TYPE, balances);
        AccountType accountType = SUPPORTED_ACCOUNT_TYPES.get(account.getCashAccountType());

        ProviderAccountDTO.ProviderAccountDTOBuilder providerAccountDTOBuilder = ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .yoltAccountType(accountType)
                .name(account.getName())
                .bic(account.getBic())
                .availableBalance(availableBalance.orElseGet(currentBalance::get))
                .currentBalance(currentBalance.orElseGet(availableBalance::get))
                .transactions(mapToTransactions(transactions))
                .lastRefreshed(ZonedDateTime.now(clock))
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban()))
                .extendedAccount(mapToExtendedAccount(account, balances));
        if (accountType.equals(AccountType.CREDIT_CARD)) {
            providerAccountDTOBuilder.creditCardData(ProviderCreditCardDTO.builder()
                    .availableCreditAmount(availableBalance.orElseGet(currentBalance::get))
                    .build());
        }

        return providerAccountDTOBuilder.build();
    }

    private ExtendedAccountDTO mapToExtendedAccount(final UniCreditAccountDTO account,
                                                    final List<UniCreditBalanceDTO> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(mapToBalances(balances))
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .cashAccountType(ExternalCashAccountType.fromCode(account.getCashAccountType()))
                .bic(account.getBic())
                .build();
    }

    private List<BalanceDTO> mapToBalances(final List<UniCreditBalanceDTO> balances) {
        return balances.stream().map(balance -> BalanceDTO.builder()
                .balanceAmount(new BalanceAmountDTO(currencyCodeMapper.toCurrencyCode(balance.getCurrency()), BigDecimal.valueOf(balance.getAmount())))
                .balanceType(BalanceType.fromName(balance.getBalanceType()))
                .lastChangeDateTime(StringUtils.isEmpty(balance.getLastChangeDateTime()) ? null : ZonedDateTime.parse(balance.getLastChangeDateTime(), ISO_DATE_TIME))
                .referenceDate(StringUtils.isEmpty(balance.getReferenceDate()) ? null : ZonedDateTime.of(LocalDate.parse(balance.getReferenceDate(), ISO_DATE), LocalTime.MIN, ZoneId.of("Z")))
                .lastCommittedTransaction(balance.getLastCommittedTransaction())
                .build()).collect(Collectors.toList());
    }

    private List<ProviderTransactionDTO> mapToTransactions(final List<UniCreditTransactionsDTO> transactions) {
        List<ProviderTransactionDTO> mappedTransactions = new ArrayList<>();
        for (UniCreditTransactionsDTO transactionsDivided : transactions) {
            mappedTransactions.addAll(mapToTransactions(transactionsDivided.getBookedTransactions(), TransactionStatus.BOOKED));
            mappedTransactions.addAll(mapToTransactions(transactionsDivided.getPendingTransactions(), TransactionStatus.PENDING));
        }
        return mappedTransactions;
    }

    private List<ProviderTransactionDTO> mapToTransactions(final List<UniCreditTransactionDTO> transactions, final TransactionStatus status) {
        if (transactions == null)
            return Collections.emptyList();
        return transactions.stream()
                .map(transaction -> mapToTransaction(transaction, status))
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO mapToTransaction(final UniCreditTransactionDTO transaction, final TransactionStatus status) {
        ProviderTransactionDTO returnedValue = ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(parseDateTime(transaction.getBookingDate()))
                .amount(BigDecimal.valueOf(transaction.getAmount()).abs())
                .type(transaction.getAmount() < 0 ? ProviderTransactionType.DEBIT : ProviderTransactionType.CREDIT)
                .status(status)
                .merchant(transaction.getCreditorName())
                .extendedTransaction(mapToExtendedTransaction(transaction, status))
                .description(transaction.getRemittanceInformationUnstructured())
                .category(YoltCategory.GENERAL)
                .build();
        if(returnedValue.getDescription().length() > 1000) { // TODO Remove after C4PO-4882 has been investigated
            log.debug(RDD_MARKER, "Transaction description is longer than 1000, description: {}", returnedValue.getDescription());
        }
        return returnedValue;
    }

    private ExtendedTransactionDTO mapToExtendedTransaction(final UniCreditTransactionDTO transaction, final TransactionStatus status) {
        AccountReferenceDTO creditorAccount = transaction.getCreditorName() == null ? null : new AccountReferenceDTO(AccountReferenceType.IBAN, transaction.getCreditorIban());
        AccountReferenceDTO debtorAccount = transaction.getDebtorName() == null ? null : new AccountReferenceDTO(AccountReferenceType.IBAN, transaction.getDebtorIban());
        return ExtendedTransactionDTO.builder()
                .status(status)
                .entryReference(transaction.getEntryReference())
                .endToEndId(transaction.getEndToEndId())
                .mandateId(transaction.getMandateId())
                .checkId(transaction.getCheckId())
                .creditorId(transaction.getCreditorId())
                .bookingDate(parseDateTime(transaction.getBookingDate()))
                .valueDate(parseDateTime(transaction.getValueDate()))
                .transactionAmount(new BalanceAmountDTO(currencyCodeMapper.toCurrencyCode(transaction.getCurrency()), BigDecimal.valueOf(transaction.getAmount())))
                .exchangeRate(mapToExchangeRates(transaction.getExchangeRates()))
                .creditorName(transaction.getCreditorName())
                .creditorAccount(creditorAccount)
                .ultimateCreditor(transaction.getUltimateCreditor())
                .debtorName(transaction.getDebtorName())
                .debtorAccount(debtorAccount)
                .ultimateDebtor(transaction.getUltimateDebtor())
                .remittanceInformationStructured(transaction.getRemittanceInformationStructured())
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .purposeCode(transaction.getPurposeCode())
                .bankTransactionCode(transaction.getBankTransactionCode())
                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .transactionIdGenerated(false)
                .build();
    }

    private ZonedDateTime parseDateTime(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneId.of("Europe/Rome"));
    }

    private List<ExchangeRateDTO> mapToExchangeRates(final List<com.yolt.providers.unicredit.common.dto.ExchangeRateDTO> exchangeRates) {
        if (exchangeRates == null) {
            return Collections.emptyList();
        }
        return exchangeRates
                .stream()
                .map(this::mapToExchangeRate)
                .collect(Collectors.toList());
    }

    private ExchangeRateDTO mapToExchangeRate(final com.yolt.providers.unicredit.common.dto.ExchangeRateDTO exchangeRate) {
        return ExchangeRateDTO.builder()
                .currencyFrom(currencyCodeMapper.toCurrencyCode(exchangeRate.getCurrencyFrom()))
                .currencyTo(currencyCodeMapper.toCurrencyCode(exchangeRate.getCurrencyTo()))
                .rateDate(parseDateTime(exchangeRate.getRateDate()))
                .rateContract(exchangeRate.getRateContract())
                .rateFrom(exchangeRate.getRateFrom())
                .rateTo(exchangeRate.getRateTo())
                .build();
    }

    private static Optional<BigDecimal> retrieveBalance(final BalanceType type, final List<UniCreditBalanceDTO> balances) {
        for (UniCreditBalanceDTO balance : balances) {
            if (type.equals(BalanceType.fromName(balance.getBalanceType()))) {
                return Optional.of(BigDecimal.valueOf(balance.getAmount()));
            }
        }
        return Optional.empty();
    }

     @Override
    public boolean verifyAccountType(String cashAccountType) {
        return SUPPORTED_ACCOUNT_TYPES.containsKey(cashAccountType);
    }
}