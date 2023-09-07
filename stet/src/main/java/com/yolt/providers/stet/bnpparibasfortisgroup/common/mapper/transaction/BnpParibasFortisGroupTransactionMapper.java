package com.yolt.providers.stet.bnpparibasfortisgroup.common.mapper.transaction;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.List;

public class BnpParibasFortisGroupTransactionMapper extends DefaultTransactionMapper {

    public BnpParibasFortisGroupTransactionMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    protected ProviderTransactionDTO mapToProviderTransactionDTO(StetTransactionDTO transaction) {
        OffsetDateTime transactionDate = transaction.getTransactionDate() != null ? transaction.getTransactionDate() : transaction.getBookingDate();
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getEntryReference())
                .dateTime(dateTimeSupplier.convertToZonedDateTime(transactionDate))
                .amount(transaction.getAmount().abs())
                .status(mapToTransactionStatus(transaction.getStatus()))
                .type(mapToTransactionType(transaction.getTransactionIndicator()))
                .extendedTransaction(mapToExtendedTransactionDTO(transaction))
                .description(mapToDescription(transaction.getUnstructuredRemittanceInformation()))
                .category(YoltCategory.GENERAL)
                .build();
    }

    @Override
    public ExtendedTransactionDTO mapToExtendedTransactionDTO(StetTransactionDTO transaction) {
        return ExtendedTransactionDTO.builder()
                .remittanceInformationUnstructured(mapToDescription(transaction.getUnstructuredRemittanceInformation()))
                .bookingDate(dateTimeSupplier.convertToZonedDateTime(transaction.getBookingDate()))
                .valueDate(dateTimeSupplier.convertToZonedDateTime(transaction.getValueDate()))
                .status(mapToTransactionStatus(transaction.getStatus()))
                .transactionAmount(mapToBalanceAmountDTO(transaction))
                .entryReference(transaction.getEntryReference())
                .build();
    }

    @Override
    protected String mapToDescription(List<String> unstructuredRemittanceInformation) {
        if (CollectionUtils.isEmpty(unstructuredRemittanceInformation)) {
            return "N/A";
        }
        return super.mapToDescription(unstructuredRemittanceInformation);
    }
}
