package com.yolt.providers.triodosbank.common.mapper;

import com.yolt.providers.triodosbank.common.model.Account;
import com.yolt.providers.triodosbank.common.model.AmountType;
import com.yolt.providers.triodosbank.common.model.Transaction;
import com.yolt.providers.triodosbank.common.model.Transactions;
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
import org.springframework.util.NumberUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TriodosBankTransactionMapper {

    private final Clock clock;

    public List<ProviderTransactionDTO> mapProviderTransactionsDTO(Transactions transactions) {
        List<ProviderTransactionDTO> bookedTransactions = transactions.getBooked().stream()
                .map(transaction -> mapProviderTransactionDTO(transaction, TransactionStatus.BOOKED))
                .collect(Collectors.toList());

        List<ProviderTransactionDTO> pendingTransactions = transactions.getPending().stream()
                .map(transaction -> mapProviderTransactionDTO(transaction, TransactionStatus.PENDING))
                .collect(Collectors.toList());

        List<ProviderTransactionDTO> allTransactions = new ArrayList<>();
        allTransactions.addAll(bookedTransactions);
        allTransactions.addAll(pendingTransactions);
        return allTransactions;
    }

    private ProviderTransactionDTO mapProviderTransactionDTO(Transaction transaction, TransactionStatus status) {
        ProviderTransactionType transactionType = toProviderTransactionType(transaction, status);
        return ProviderTransactionDTO.builder()
                .externalId(Objects.isNull(transaction.getTransactionId()) ? null : transaction.getTransactionId())
                .dateTime(toNullableZonedDateTime(status.equals(TransactionStatus.BOOKED) ? transaction.getValueDate() : transaction.getBookingDate()))
                .type(transactionType)
                .category(YoltCategory.GENERAL)
                .amount(toTransactionAmount(transaction.getTransactionAmount()))
                .description(Optional.ofNullable(transaction.getRemittanceInformationUnstructured()).orElse(""))
                .status(status)
                .extendedTransaction(mapExtendedTransactionDTO(transaction, status, transactionType))
                .build();
    }

    private ProviderTransactionType toProviderTransactionType(Transaction transaction, TransactionStatus status) {
        if (StringUtils.isNotEmpty(transaction.getCreditorName()) || Objects.nonNull(transaction.getCreditorAccount())) {
            if (isPendingRefundTransaction(transaction, status)) {
                return ProviderTransactionType.CREDIT;
            } else {
                return ProviderTransactionType.DEBIT;
            }
        }
        if (StringUtils.isNotEmpty(transaction.getDebtorName()) || Objects.nonNull(transaction.getDebtorAccount())) {
            return ProviderTransactionType.CREDIT;
        }
        return null;
    }

    private BigDecimal toTransactionAmount(AmountType transactionAmount) {
        return new BigDecimal(transactionAmount.getAmount()).abs();
    }

    private Boolean isPendingRefundTransaction(Transaction transaction, TransactionStatus status) {
        return TransactionStatus.PENDING.equals(status) && isRefund(transaction);
    }

    private boolean isRefund(Transaction transaction) {
        return NumberUtils.parseNumber(transaction.getTransactionAmount().getAmount(), BigDecimal.class).compareTo(BigDecimal.ZERO) < 0;
    }

    private ExtendedTransactionDTO mapExtendedTransactionDTO(Transaction transaction,
                                                             TransactionStatus status,
                                                             ProviderTransactionType transactionType) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .bookingDate(transaction.getBookingDate() != null ? toNullableZonedDateTime(transaction.getBookingDate()) : null)
                .valueDate(transaction.getValueDate() != null ? toNullableZonedDateTime(transaction.getValueDate()) : null)
                .status(status)
                .transactionAmount(mapBalanceAmountDTO(transaction.getTransactionAmount(), transactionType))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .transactionIdGenerated(true);

        if (Objects.nonNull(transaction.getCreditorAccount())) {
            builder.creditorName(transaction.getCreditorName())
                    .creditorAccount(mapAccountReferenceDTO(transaction.getCreditorAccount()));
        }
        if (Objects.nonNull(transaction.getDebtorAccount())) {
            builder.debtorName(transaction.getDebtorName())
                    .debtorAccount(mapAccountReferenceDTO(transaction.getDebtorAccount()));
        }
        return builder.build();
    }

    private BalanceAmountDTO mapBalanceAmountDTO(AmountType amount, ProviderTransactionType transactionType) {
        return BalanceAmountDTO.builder()
                .amount(toAdjustedTransactionAmount(amount, transactionType))
                .currency(toCurrencyCode(amount.getCurrency()))
                .build();
    }

    private BigDecimal toAdjustedTransactionAmount(AmountType transactionAmount, ProviderTransactionType transactionType) {
        BigDecimal amount = new BigDecimal(transactionAmount.getAmount());
        if (ProviderTransactionType.CREDIT.equals(transactionType)) {
            return amount.abs();
        }
        return amount.abs().negate();
    }

    private static CurrencyCode toCurrencyCode(final String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private ZonedDateTime toNullableZonedDateTime(String dateTime) {
        if (StringUtils.isNotEmpty(dateTime)) {
            return ZonedDateTime.from(LocalDate.parse(dateTime).atStartOfDay(clock.getZone()));
        }
        return null;
    }

    private AccountReferenceDTO mapAccountReferenceDTO(Account account) {
        return AccountReferenceDTO.builder()
                .type(AccountReferenceType.IBAN)
                .value(account.getIban())
                .build();
    }
}
