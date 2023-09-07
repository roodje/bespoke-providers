package com.yolt.providers.stet.cicgroup.common.mapper.transaction;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.YoltCategory;

import java.time.OffsetDateTime;

public class CicGroupTransactionMapper extends DefaultTransactionMapper {

    protected final DateTimeSupplier dateTimeSupplier;

    public CicGroupTransactionMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
        this.dateTimeSupplier = dateTimeSupplier;
    }

    @Override
    protected ProviderTransactionDTO mapToProviderTransactionDTO(StetTransactionDTO transaction) {
        OffsetDateTime transactionDateTime = transaction.getBookingDate() == null ?
                transaction.getExpectedBookingDate() :
                transaction.getBookingDate();

        return ProviderTransactionDTO.builder()
                .externalId(transaction.getResourceId())
                .amount(mapToTransactionAmount(transaction.getAmount()))
                .category(YoltCategory.GENERAL)
                .dateTime(dateTimeSupplier.convertToZonedDateTime(transactionDateTime))
                .description(mapToDescription(transaction.getUnstructuredRemittanceInformation()))
                .type(mapToTransactionType(transaction.getTransactionIndicator()))
                .extendedTransaction(mapToExtendedTransactionDTO(transaction))
                .status(mapToTransactionStatus(transaction.getStatus()))
                .build();
    }

    @Override
    public ExtendedTransactionDTO mapToExtendedTransactionDTO(StetTransactionDTO transaction) {
        return ExtendedTransactionDTO.builder()
                .status(mapToTransactionStatus(transaction.getStatus()))
                .endToEndId(transaction.getEndToEndId())
                .entryReference(transaction.getResourceId())
                .bookingDate(dateTimeSupplier.convertToZonedDateTime(transaction.getBookingDate() == null ?
                        transaction.getExpectedBookingDate() :
                        transaction.getBookingDate()))
                .valueDate(dateTimeSupplier.convertToZonedDateTime(transaction.getValueDate() == null ?
                        null :
                        transaction.getValueDate()))
                .transactionAmount(mapToTransactionAmountDTO(transaction))
                .remittanceInformationUnstructured(mapToDescription(transaction.getUnstructuredRemittanceInformation()))
                .bankTransactionCode(mapToBankTransactionCode(transaction))
                .proprietaryBankTransactionCode(transaction.getBankTransactionCode())
                .creditorId(transaction.getCreditorIdentification())
                .creditorName(transaction.getCreditorName())
                .creditorAccount(mapToAccountReferenceDTOs(transaction.getCreditorIban()))
                .ultimateCreditor(transaction.getUltimateCreditorName())
                .debtorName(transaction.getDebtorName())
                .debtorAccount(mapToAccountReferenceDTOs(transaction.getDebtorIban()))
                .ultimateDebtor(transaction.getUltimateDebtorName())
                .build();
    }

    private BalanceAmountDTO mapToTransactionAmountDTO(StetTransactionDTO transaction) {
        return BalanceAmountDTO.builder()
                .amount(adjustSignIndicator(transaction.getAmount(), transaction.getTransactionIndicator()))
                .currency(transaction.getCurrency())
                .build();
    }
}
