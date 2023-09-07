package com.yolt.providers.stet.societegeneralegroup.common.mapper.transaction;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

public class SocieteGeneraleTransactionMapper extends DefaultTransactionMapper {

    public SocieteGeneraleTransactionMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    protected ProviderTransactionDTO mapToProviderTransactionDTO(StetTransactionDTO transaction) {
        OffsetDateTime transactionDateTime = transaction.getBookingDate() == null ?
                transaction.getExpectedBookingDate() :
                transaction.getBookingDate();

        return ProviderTransactionDTO.builder()
                .externalId(transaction.getEntryReference())
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
        OffsetDateTime transactionDateTime = transaction.getBookingDate() == null ?
                transaction.getExpectedBookingDate() :
                transaction.getBookingDate();

        return ExtendedTransactionDTO.builder()
                .status(mapToTransactionStatus(transaction.getStatus()))
                .endToEndId(transaction.getEndToEndId())
                .entryReference(transaction.getEntryReference())
                .bookingDate(dateTimeSupplier.convertToZonedDateTime(transactionDateTime))
                .valueDate(transaction.getValueDate() != null
                        ? dateTimeSupplier.convertToZonedDateTime(transaction.getValueDate())
                        : null)
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
    protected BigDecimal mapToTransactionAmount(BigDecimal amount) {
        return ObjectUtils.isEmpty(amount) ? null : amount.abs().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    protected BalanceAmountDTO mapToBalanceAmountDTO(StetTransactionDTO transaction) {
        BigDecimal transactionAmount = transaction.getAmount();
        if (transactionAmount != null) {
            transactionAmount = transactionAmount.setScale(2, RoundingMode.HALF_UP);
        }
        return BalanceAmountDTO.builder()
                .amount(adjustSignIndicator(transactionAmount, transaction.getTransactionIndicator()))
                .currency(transaction.getCurrency())
                .build();
    }

    @Override
    protected String mapToDescription(List<String> unstructuredRemittanceInformation) {
        return String.join(", ", unstructuredRemittanceInformation).trim();
    }
}
