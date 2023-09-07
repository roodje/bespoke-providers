package com.yolt.providers.monorepogroup.olbgroup.common.mapper;

import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata.Transaction;
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
public class OlbGroupTransactionMapper {

    private final OlbGroupDateConverter dateConverter;

    public List<ProviderTransactionDTO> mapProviderTransactionsDTO(List<Transaction> transactions, TransactionStatus status) {
        if (Objects.isNull(transactions)) {
            return Collections.emptyList();
        }
        return transactions.stream()
                .map(transaction -> mapProviderTransactionDTO(transaction, status))
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO mapProviderTransactionDTO(Transaction transaction, TransactionStatus status) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(mapToZonedDateTime(transaction.getBookingDate()))
                .type(mapToProviderTransactionType(transaction))
                .category(YoltCategory.GENERAL)
                .amount(transaction.getAmount().abs())
                .description(defaultIfEmpty(transaction.getRemittanceInformationUnstructured(), "N/A"))
                .status(status)
                .extendedTransaction(mapExtendedTransactionDTO(transaction, status))
                .build();
    }

    private static ProviderTransactionType mapToProviderTransactionType(Transaction transaction) {
        return transaction.getAmount().compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT;
    }

    private ExtendedTransactionDTO mapExtendedTransactionDTO(Transaction transaction,
                                                             TransactionStatus status) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bookingDate(mapToZonedDateTime(transaction.getBookingDate()))
                .valueDate(mapToZonedDateTime(transaction.getValueDate()))
                .status(status)
                .transactionAmount(mapBalanceAmountDTO(transaction))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .transactionIdGenerated(true);

        if (Objects.nonNull(transaction.getCreditorName())) {
            builder.creditorName(transaction.getCreditorName())
                    .creditorAccount(mapAccountReferenceDTO(transaction.getCreditorIban()));
        }
        if (Objects.nonNull(transaction.getDebtorName())) {
            builder.debtorName(transaction.getDebtorName())
                    .debtorAccount(mapAccountReferenceDTO(transaction.getDebtorIban()));
        }
        return builder.build();
    }

    private ZonedDateTime mapToZonedDateTime(String dateTime) {
        return dateConverter.getNullableZonedDateTime(dateTime);
    }

    private BalanceAmountDTO mapBalanceAmountDTO(Transaction transaction) {
        return BalanceAmountDTO.builder()
                .amount(transaction.getAmount())
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
