package com.yolt.providers.stet.generic.mapper.transaction;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionIndicator;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultTransactionMapper implements TransactionMapper {

    protected final DateTimeSupplier dateTimeSupplier;

    @Override
    public List<ProviderTransactionDTO> mapToProviderTransactionDTOs(List<StetTransactionDTO> transactions) {
        return transactions.stream()
                .map(this::mapToProviderTransactionDTO)
                .collect(Collectors.toList());
    }

    protected ProviderTransactionDTO mapToProviderTransactionDTO(StetTransactionDTO transaction) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getEntryReference())
                .amount(mapToTransactionAmount(transaction.getAmount()))
                .category(YoltCategory.GENERAL)
                .dateTime(dateTimeSupplier.convertToZonedDateTime(transaction.getBookingDate()))
                .description(mapToDescription(transaction.getUnstructuredRemittanceInformation()))
                .type(mapToTransactionType(transaction.getTransactionIndicator()))
                .extendedTransaction(mapToExtendedTransactionDTO(transaction))
                .status(mapToTransactionStatus(transaction.getStatus()))
                .build();
    }

    protected String mapToDescription(List<String> unstructuredRemittanceInformation) {
        return String.join(", ", unstructuredRemittanceInformation);
    }

    protected ProviderTransactionType mapToTransactionType(StetTransactionIndicator indicator) {
        if (indicator != null) {
            switch (indicator) {
                case CRDT:
                    return ProviderTransactionType.CREDIT;
                case DBIT:
                    return ProviderTransactionType.DEBIT;
                default:
            }
        }
        return null;
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

    protected TransactionStatus mapToTransactionStatus(StetTransactionStatus transactionStatus) {
        if (transactionStatus != null) {
            switch (transactionStatus) {
                case BOOK:
                    return TransactionStatus.BOOKED;
                case PDNG:
                case OTHR:
                    return TransactionStatus.PENDING;
                default:
            }
        }
        return null;
    }

    protected BalanceAmountDTO mapToBalanceAmountDTO(StetTransactionDTO transaction) {
        return BalanceAmountDTO.builder()
                .amount(adjustSignIndicator(transaction.getAmount(), transaction.getTransactionIndicator()))
                .currency(transaction.getCurrency())
                .build();
    }

    protected BigDecimal adjustSignIndicator(BigDecimal amount, StetTransactionIndicator indicator) {
        if (amount == null) {
            return null;
        }
        BigDecimal absoluteAmount = amount.abs();
        if (StetTransactionIndicator.DBIT.equals(indicator)) {
            return absoluteAmount.negate();
        }
        return absoluteAmount;
    }

    protected String mapToBankTransactionCode(StetTransactionDTO transaction) {
        ArrayList<String> components = new ArrayList<>();
        components.add(transaction.getBankTransactionDomain());
        components.add(transaction.getBankTransactionFamily());
        components.add(transaction.getBankTransactionSubfamily());
        components.removeIf(StringUtils::isEmpty);
        return components.isEmpty() ? null : String.join(",", components);
    }

    protected AccountReferenceDTO mapToAccountReferenceDTOs(String iban) {
        if (StringUtils.isNotEmpty(iban)) {
            return AccountReferenceDTO.builder()
                    .type(AccountReferenceType.IBAN)
                    .value(iban.replace(" ", ""))
                    .build();
        }
        return null;
    }

    protected BigDecimal mapToTransactionAmount(BigDecimal amount) {
        return ObjectUtils.isEmpty(amount) ? null : amount.abs();
    }
}
