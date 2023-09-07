package com.yolt.providers.axabanque.common.fetchdata.mapper;

import com.yolt.providers.axabanque.common.model.external.Account;
import com.yolt.providers.axabanque.common.model.external.Transactions;
import com.yolt.providers.axabanque.common.model.external.Transactions.Transaction;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class DefaultTransactionMapper implements TransactionMapper {

    private final ZoneId zoneId;

    @Override
    public List<ProviderTransactionDTO> mapToTransactions(final List<Transactions> transactions) {
        if (transactions == null) {
            return Collections.emptyList();
        }
        Stream<ProviderTransactionDTO> bookedTransactions = getProviderTransactionStream(
                transactions
                        .stream()
                        .filter(t -> t.getBookedTransactions() != null)
                        .flatMap(t -> t.getBookedTransactions().stream()),
                TransactionStatus.BOOKED);

        Stream<ProviderTransactionDTO> pendingTransactions = getProviderTransactionStream(
                transactions
                        .stream()
                        .filter(t -> t.getPendingTransactions() != null)
                        .flatMap(t -> t.getPendingTransactions().stream())
                , TransactionStatus.PENDING);

        return Stream.concat(pendingTransactions, bookedTransactions)
                .collect(Collectors.toList());
    }

    private Stream<ProviderTransactionDTO> getProviderTransactionStream(Stream<Transaction> transactions, TransactionStatus status) {
        return transactions == null ? Stream.empty() :
                transactions
                        .map(transaction -> this.convertTransaction(transaction, status));
    }

    private ProviderTransactionDTO convertTransaction(Transaction transaction, TransactionStatus transactionStatus) {
        String valueDate = transaction.getValueDate();
        BigDecimal amount = transaction.getAmount() == null ? null : BigDecimal.valueOf(transaction.getAmount().getAmount()).abs();
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(valueDate == null ? null : toNullableZonedDateTime(valueDate))
                .amount(amount)
                .type(mapToTransactionType(BigDecimal.valueOf(transaction.getAmount().getAmount())))
                .status(transactionStatus)
                .description(transaction.getRemittanceInformationUnstructured())
                .category(YoltCategory.GENERAL)
                .merchant(transaction.getCreditorName())
                .extendedTransaction(mapExtendedTransaction(transaction, transactionStatus))
                .build();
    }

    private ProviderTransactionType mapToTransactionType(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.compareTo(BigDecimal.ZERO) > 0 ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT;
    }

    private ZonedDateTime toNullableZonedDateTime(String dateTime) {
        if (StringUtils.isNotEmpty(dateTime)) {
            return ZonedDateTime.from(LocalDate.parse(dateTime).atStartOfDay(zoneId));
        }
        return null;
    }

    private ExtendedTransactionDTO mapExtendedTransaction(final Transaction transaction, TransactionStatus transactionStatus) {
        ZonedDateTime bookingDate = transaction.getBookingDate() == null ? null : toNullableZonedDateTime(transaction.getBookingDate());
        ZonedDateTime valueDate = transaction.getValueDate() == null ? null : toNullableZonedDateTime(transaction.getValueDate());
        return ExtendedTransactionDTO.builder()
                .status(transactionStatus)
                .transactionIdGenerated(false)
                .endToEndId(transaction.getEndToEndId())
                .bookingDate(bookingDate)
                .valueDate(valueDate)
                .transactionAmount(getTransactionAmount(transaction))
                .creditorName(transaction.getCreditorName())
                .creditorAccount(retrieveAccountNumber(transaction.getCreditorAccount()))
                .debtorName(transaction.getDebtorName())
                .debtorAccount(retrieveAccountNumber(transaction.getDebtorAccount()))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .remittanceInformationStructured(transaction.getRemittanceInformationStructured())
                .creditorId(transaction.getCreditorId())
                .mandateId(transaction.getMandateId())
                .purposeCode(transaction.getPurposeCode())
                .ultimateCreditor(transaction.getUltimateCreditor())
                .ultimateDebtor(transaction.getUltimateDebtor())
                .bankTransactionCode(transaction.getBankTransactionCode())
                .build();
    }

    private BalanceAmountDTO getTransactionAmount(Transaction transaction) {
        if (transaction.getAmount() == null) {
            return null;
        }
        return new BalanceAmountDTO(toCurrencyCode(transaction.getAmount().getCurrency()), BigDecimal.valueOf(transaction.getAmount().getAmount()));
    }

    private CurrencyCode toCurrencyCode(final String currencyCode) {
        if (StringUtils.isEmpty(currencyCode)) {
            return null;
        }
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    private AccountReferenceDTO retrieveAccountNumber(final Account account) {
        if (account == null) {
            return null;
        }
        if (account.getIban() != null) {
            return new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban());
        }
        return null;
    }

}
