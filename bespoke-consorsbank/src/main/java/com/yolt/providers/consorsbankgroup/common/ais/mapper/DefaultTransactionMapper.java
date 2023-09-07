package com.yolt.providers.consorsbankgroup.common.ais.mapper;

import com.yolt.providers.consorsbankgroup.dto.AccountReport;
import com.yolt.providers.consorsbankgroup.dto.TransactionDetails;
import com.yolt.providers.consorsbankgroup.dto.TransactionsResponse200Json;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yolt.providers.consorsbankgroup.consorsbank.ConsorsbankBeanConfig.ZONE_ID;

@RequiredArgsConstructor
public class DefaultTransactionMapper {

    private final DefaultExtendedModelTransactionMapper extendedModelTransactionMapper;

    public List<ProviderTransactionDTO> mapTransactions(final TransactionsResponse200Json transactionsResponse) {
        List<ProviderTransactionDTO> mappedTransactions = new ArrayList<>();
        AccountReport accountReport = transactionsResponse.getTransactions();
        if (accountReport.getBooked() != null) {
            var transactions = accountReport.getBooked()
                    .stream()
                    .map(t -> mapTransaction(t, TransactionStatus.BOOKED))
                    .collect(Collectors.toList());
            mappedTransactions.addAll(transactions);
        }
        if (accountReport.getPending() != null) {
            var transactions = accountReport.getPending()
                    .stream()
                    .map(t -> mapTransaction(t, TransactionStatus.PENDING))
                    .collect(Collectors.toList());
            mappedTransactions.addAll(transactions);
        }
        return mappedTransactions;
    }

    private ProviderTransactionDTO mapTransaction(final TransactionDetails transactionDetails, final TransactionStatus status) {
        BigDecimal amount = new BigDecimal(transactionDetails.getTransactionAmount().getAmount());
        return ProviderTransactionDTO.builder()
                .externalId(transactionDetails.getTransactionId())
                .description(transactionDetails.getRemittanceInformationUnstructured())
                .dateTime(transactionDetails.getBookingDate() == null ? transactionDetails.getValueDate().atStartOfDay(ZONE_ID) : transactionDetails.getBookingDate().atStartOfDay(ZONE_ID))
                .type(amount.compareTo(BigDecimal.ZERO) > 0 ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT)
                .category(YoltCategory.GENERAL)
                .amount(amount.abs())
                .status(status)
                .extendedTransaction(extendedModelTransactionMapper.mapTransaction(transactionDetails, status))
                .build();
    }
}
