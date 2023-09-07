package com.yolt.providers.deutschebank.common.mapper;

import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupDateConverter;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.Transaction;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@RequiredArgsConstructor
public class DeutscheBankGroupTransactionMapper {

    private final DeutscheBankGroupDateConverter dateConverter;

    public List<ProviderTransactionDTO> mapProviderTransactionsDTO(List<Transaction> transactions, TransactionStatus status, String accountIban, String accountName) {
        if (Objects.isNull(transactions)) {
            return Collections.emptyList();
        }
        return transactions.stream()
                .map(transaction -> mapProviderTransactionDTO(transaction, status, accountIban, accountName))
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO mapProviderTransactionDTO(Transaction transaction, TransactionStatus status, String accountIban, String accountName) {
        ProviderTransactionType type = mapToProviderTransactionType(transaction);
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(mapToZonedDateTime(transaction.getBookingDate(), transaction.getTransactionId()))
                .type(type)
                .category(YoltCategory.GENERAL)
                .amount(transaction.getDecimalAmount().abs())
                .description(defaultIfEmpty(transaction.getRemittanceInformationUnstructured(), "N/A"))
                .status(status)
                .extendedTransaction(mapExtendedTransactionDTO(transaction, status, type, accountIban, accountName))
                .build();
    }

    private static ProviderTransactionType mapToProviderTransactionType(Transaction transaction) {
        return new BigDecimal(transaction.getAmount()).compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT;
    }

    private ExtendedTransactionDTO mapExtendedTransactionDTO(Transaction transaction,
                                                             TransactionStatus status,
                                                             ProviderTransactionType type,
                                                             String accountIban,
                                                             String accountName) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bookingDate(mapToZonedDateTime(transaction.getBookingDate(), transaction.getTransactionId()))
                .valueDate(mapToZonedDateTime(transaction.getValueDate(), transaction.getTransactionId()))
                .status(status)
                .transactionAmount(mapBalanceAmountDTO(transaction))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .transactionIdGenerated(true);

        if (Objects.nonNull(transaction.getCreditorName())) {
            builder.creditorName(transaction.getCreditorName())
                    .creditorAccount(mapAccountReferenceDTO(transaction.getCreditorIban()));
        } else {
            if (Objects.nonNull(accountIban)) {
                if (CREDIT.equals(type)) {
                    builder.creditorName(accountName)
                            .creditorAccount(mapAccountReferenceDTO(accountIban));
                }
            }
        }
        if (Objects.nonNull(transaction.getDebtorName())) {
            builder.debtorName(transaction.getDebtorName())
                    .debtorAccount(mapAccountReferenceDTO(transaction.getDebtorIban()));

        } else {
            if (Objects.nonNull(accountIban)) {
                if (DEBIT.equals(type)) {
                    builder.debtorName(accountName)
                            .debtorAccount(mapAccountReferenceDTO(accountIban));
                }
            }
        }
        return builder.build();
    }

    private ZonedDateTime mapToZonedDateTime(String dateTime, String transactionId) {
        ZonedDateTime zonedDateTime = dateConverter.getNullableZonedDateTime(dateTime);
        if (zonedDateTime != null) {
            return zonedDateTime;
        }
        if (transactionId != null) {
            return dateConverter.getZonedDateTimeWhenDateTimeIsMissing(transactionId);
        }
        return null;
    }

    private BalanceAmountDTO mapBalanceAmountDTO(Transaction transaction) {
        return BalanceAmountDTO.builder()
                .amount(transaction.getDecimalAmount())
                .currency(mapToCurrencyCode(transaction.getCurrency()))
                .build();
    }

    private static CurrencyCode mapToCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private AccountReferenceDTO mapAccountReferenceDTO(String iban) {
        return AccountReferenceDTO.builder()
                .type(AccountReferenceType.IBAN)
                .value(iban)
                .build();
    }
}
