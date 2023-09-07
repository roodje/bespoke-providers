package com.yolt.providers.stet.bpcegroup.common.mapper.transaction;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class BpceGroupTransactionMapper extends DefaultTransactionMapper {

    public BpceGroupTransactionMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    protected ProviderTransactionDTO mapToProviderTransactionDTO(StetTransactionDTO transaction) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getResourceId())
                .amount(mapToTransactionAmount(transaction.getAmount()))
                .category(YoltCategory.GENERAL)
                .dateTime(dateTimeSupplier.convertToZonedDateTime(transaction.getBookingDate()))
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
                .entryReference(transaction.getEntryReference())
                .bookingDate(dateTimeSupplier.convertToZonedDateTime(transaction.getBookingDate()))
                .valueDate(dateTimeSupplier.convertToZonedDateTime(transaction.getValueDate()))
                .transactionAmount(mapToBalanceAmountDTO(transaction))
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
                .transactionIdGenerated(false)
                .build();
    }

    @Override
    protected String mapToDescription(List<String> unstructuredRemittanceInformation) {
        if (CollectionUtils.isEmpty(unstructuredRemittanceInformation)) {
            return "N/A";
        }
        return String.join(" ", unstructuredRemittanceInformation);
    }
}
