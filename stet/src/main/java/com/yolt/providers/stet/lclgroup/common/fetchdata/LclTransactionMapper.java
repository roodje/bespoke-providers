package com.yolt.providers.stet.lclgroup.common.fetchdata;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.YoltCategory;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class LclTransactionMapper extends DefaultTransactionMapper {

    final LclDateTimeSupplier dateTimeSupplier;

    public LclTransactionMapper(final LclDateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
        this.dateTimeSupplier = dateTimeSupplier;
    }

    @Override
    protected ProviderTransactionDTO mapToProviderTransactionDTO(final StetTransactionDTO transaction) {
        OffsetDateTime transactionDate = transaction.getValueDate() != null ? transaction.getValueDate() : transaction.getBookingDate();

        ZonedDateTime zonedDateTime = dateTimeSupplier.convertToZonedDateTime(transactionDate);
        if (zonedDateTime == null) {
            throw new LclGroupMappingException("There is no any date which can be mapped");
        }
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getResourceId())
                .dateTime(zonedDateTime)
                .amount(transaction.getAmount().abs())
                .status(mapToTransactionStatus(transaction.getStatus()))
                .type(mapToTransactionType(transaction.getTransactionIndicator()))
                .extendedTransaction(mapToExtendedTransactionDTO(transaction))
                .description(mapToDescription(transaction.getUnstructuredRemittanceInformation()))
                .category(YoltCategory.GENERAL)
                .build();
    }

    @Override
    protected TransactionStatus mapToTransactionStatus(final StetTransactionStatus transactionStatus) {
        if (transactionStatus == null) {
            return null;
        }
        switch (transactionStatus) {
            case BOOK:
                return TransactionStatus.BOOKED;
            case PDNG:
                return TransactionStatus.PENDING;
            default:
                return null;
        }
    }

    @Override
    protected String mapToDescription(final List<String> unstructuredRemittanceInformation) {
        return unstructuredRemittanceInformation.isEmpty() ? "" : String.join(" ", unstructuredRemittanceInformation);
    }

}
