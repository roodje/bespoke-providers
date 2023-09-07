package com.yolt.providers.monorepogroup.qontogroup.common.mapper;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class DefaultQontoGroupTransactionMapper implements QontoGroupTransactionMapper {

    private static final String DEBIT = "debit";
    private static final String CREDIT = "credit";
    private static final String PENDING = "pending";
    private static final String COMPLETED = "completed";

    private final QontoGroupDateMapper dateMapper;

    @Override
    public ProviderTransactionDTO map(Transaction transaction) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(ObjectUtils.isNotEmpty(transaction.getSettledAt()) ? dateMapper.toZonedDateTime(transaction.getSettledAt()) : null)
                .type(mapToType(transaction.getSide()))
                .category(YoltCategory.GENERAL)
                .amount(transaction.getAmount())
                .description(StringUtils.isNotEmpty(transaction.getReference()) ? transaction.getReference() : "N/A")
                .status(mapToStatus(transaction.getStatus()))
                .extendedTransaction(mapToExtendedTransaction(transaction))
                .build();
    }

    private ProviderTransactionType mapToType(String side) {
        return switch (side) {
            case DEBIT -> ProviderTransactionType.DEBIT;
            case CREDIT -> ProviderTransactionType.CREDIT;
            default -> null;
        };
    }

    private TransactionStatus mapToStatus(String status) {
        return switch (status) {
            case PENDING -> TransactionStatus.PENDING;
            case COMPLETED -> TransactionStatus.BOOKED;
            default -> null;
        };
    }

    private ExtendedTransactionDTO mapToExtendedTransaction(Transaction transaction) {
        var extendedTransactionBuilder = ExtendedTransactionDTO.builder()
                .bookingDate(ObjectUtils.isNotEmpty(transaction.getSettledAt()) ? dateMapper.toZonedDateTime(transaction.getSettledAt()) : null)
                .valueDate(ObjectUtils.isNotEmpty(transaction.getEmittedAt()) ? dateMapper.toZonedDateTime(transaction.getEmittedAt()) : null)
                .status(mapToStatus(transaction.getStatus()))
                .transactionAmount(BalanceAmountDTO.builder()
                        .currency(mapToCurrencyCode(transaction.getCurrency()))
                        .amount(DEBIT.equals(transaction.getSide()) ? transaction.getAmount().negate() : transaction.getAmount())
                        .build())
                .remittanceInformationUnstructured(transaction.getReference())
                .transactionIdGenerated(false);

        if (DEBIT.equals(transaction.getSide())) {
            extendedTransactionBuilder.creditorName(transaction.getLabel());
        } else if (CREDIT.equals(transaction.getSide())) {
            extendedTransactionBuilder.debtorName(transaction.getLabel());
        }

        return extendedTransactionBuilder.build();
    }

    private CurrencyCode mapToCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
