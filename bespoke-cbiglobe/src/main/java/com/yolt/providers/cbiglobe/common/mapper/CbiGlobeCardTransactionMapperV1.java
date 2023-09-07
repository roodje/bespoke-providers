package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.common.util.CbiGlobeDateUtil;
import com.yolt.providers.cbiglobe.dto.ReadCardAccountTransactionListResponseTypeTransactions;
import com.yolt.providers.cbiglobe.dto.ReadCardAccountTransactionListResponseTypeTransactionsBooked;
import com.yolt.providers.cbiglobe.dto.ReadCardAccountTransactionListResponseTypeTransactionsPending;
import com.yolt.providers.cbiglobe.dto.ReadCardAccountTransactionListResponseTypeTransactionsTransactionAmount;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class CbiGlobeCardTransactionMapperV1 implements CbiGlobeCardTransactionMapper {

    private final CurrencyCodeMapper currencyCodeMapper;

    @Override
    public List<ProviderTransactionDTO> mapToProviderTransactionDTOs(ReadCardAccountTransactionListResponseTypeTransactions transactions) {
        List<ProviderTransactionDTO> providerTransactionDTOs = new ArrayList<>();

        if (Objects.isNull(transactions)) {
            return providerTransactionDTOs;
        }
        if (!CollectionUtils.isEmpty(transactions.getBooked())) {
            for (ReadCardAccountTransactionListResponseTypeTransactionsBooked bookedTransaction : transactions.getBooked()) {
                providerTransactionDTOs.add(mapToProviderTransactionDTO(bookedTransaction));
            }
        }
        if (!CollectionUtils.isEmpty(transactions.getPending())) {
            for (ReadCardAccountTransactionListResponseTypeTransactionsPending pendingTransaction : transactions.getPending()) {
                providerTransactionDTOs.add(mapToProviderTransactionDTO(pendingTransaction));
            }
        }
        return providerTransactionDTOs;
    }

    private ProviderTransactionDTO mapToProviderTransactionDTO(ReadCardAccountTransactionListResponseTypeTransactionsBooked bookedTransaction) {
        return ProviderTransactionDTO.builder()
                .externalId(bookedTransaction.getCardTransactionId())
                .dateTime(bookedTransaction.getTransactionDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(bookedTransaction.getBookingDate()) : null)
                .amount(new BigDecimal(bookedTransaction.getTransactionAmount().getAmount()).abs())
                .status(TransactionStatus.BOOKED)
                .type(detectTransactionType(bookedTransaction.getTransactionAmount().getAmount()))
                .description(bookedTransaction.getTransactionDetails())
                .category(YoltCategory.GENERAL)
                .extendedTransaction(mapToExtendedTransactionDTO(bookedTransaction))
                .build();
    }

    private ExtendedTransactionDTO mapToExtendedTransactionDTO(ReadCardAccountTransactionListResponseTypeTransactionsBooked bookedTransaction) {
        return ExtendedTransactionDTO.builder()
                .valueDate(bookedTransaction.getTransactionDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(bookedTransaction.getTransactionDate()) : null)
                .bookingDate(bookedTransaction.getBookingDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(bookedTransaction.getBookingDate()) : null)
                .status(TransactionStatus.BOOKED)
                .transactionAmount(mapToTransactionAmount(bookedTransaction.getTransactionAmount()))
                .remittanceInformationUnstructured(bookedTransaction.getTransactionDetails())
                .remittanceInformationStructured(bookedTransaction.getTransactionDetails())
                .build();
    }

    private ProviderTransactionDTO mapToProviderTransactionDTO(ReadCardAccountTransactionListResponseTypeTransactionsPending pendingTransaction) {
        return ProviderTransactionDTO.builder()
                .externalId(pendingTransaction.getCardTransactionId())
                .dateTime(pendingTransaction.getTransactionDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(pendingTransaction.getTransactionDate()) : null)
                .amount(new BigDecimal(pendingTransaction.getTransactionAmount().getAmount()).abs())
                .status(TransactionStatus.PENDING)
                .type(detectTransactionType(pendingTransaction.getTransactionAmount().getAmount()))
                .description(pendingTransaction.getTransactionDetails())
                .category(YoltCategory.GENERAL)
                .extendedTransaction(mapToExtendedTransactionDTO(pendingTransaction))
                .build();
    }

    private ProviderTransactionType detectTransactionType(String amount) {
        return new BigDecimal(amount).compareTo(BigDecimal.ZERO) > 0 ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT;
    }

    private ExtendedTransactionDTO mapToExtendedTransactionDTO(ReadCardAccountTransactionListResponseTypeTransactionsPending pendingTransaction) {
        return ExtendedTransactionDTO.builder()
                .valueDate(pendingTransaction.getTransactionDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(pendingTransaction.getTransactionDate()) : null)
                .bookingDate(pendingTransaction.getBookingDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(pendingTransaction.getBookingDate()) : null)
                .status(TransactionStatus.PENDING)
                .transactionAmount(mapToTransactionAmount(pendingTransaction.getTransactionAmount()))
                .remittanceInformationUnstructured(pendingTransaction.getTransactionDetails())
                .remittanceInformationStructured(pendingTransaction.getTransactionDetails())
                .build();
    }

    private BalanceAmountDTO mapToTransactionAmount(ReadCardAccountTransactionListResponseTypeTransactionsTransactionAmount amountDetails) {
        CurrencyCode currency = StringUtils.isBlank(amountDetails.getCurrency()) ? CurrencyCode.EUR : currencyCodeMapper.toCurrencyCode(amountDetails.getCurrency());
        return BalanceAmountDTO.builder()
                .currency(currency)
                .amount(new BigDecimal(amountDetails.getAmount()))
                .build();
    }
}
