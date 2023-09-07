package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.common.util.CbiGlobeDateUtil;
import com.yolt.providers.cbiglobe.dto.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
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
import java.util.StringJoiner;

@AllArgsConstructor
public class CbiGlobeTransactionMapperV2 implements CbiGlobeTransactionMapper {
    private final CurrencyCodeMapper currencyCodeMapper;

    @Override
    public List<ProviderTransactionDTO> mapToProviderTransactionDTOs(TransactionsReadaccounttransactionlistType1 transactions) {
        List<ProviderTransactionDTO> providerTransactionDTOs = new ArrayList<>();

        if (Objects.isNull(transactions)) {
            return providerTransactionDTOs;
        }
        if (!CollectionUtils.isEmpty(transactions.getBooked())) {
            for (TransactionsReadaccounttransactionlistType1BookedReadaccounttransactionlistType2 bookedTransaction : transactions.getBooked()) {
                providerTransactionDTOs.add(mapToProviderTransactionDTO(bookedTransaction));
            }
        }
        if (!CollectionUtils.isEmpty(transactions.getPending())) {
            for (TransactionsReadaccounttransactionlistType1PendingReadaccounttransactionlistType2 pendingTransaction : transactions.getPending()) {
                providerTransactionDTOs.add(mapToProviderTransactionDTO(pendingTransaction));
            }
        }
        return providerTransactionDTOs;
    }

    private ProviderTransactionDTO mapToProviderTransactionDTO(TransactionsReadaccounttransactionlistType1BookedReadaccounttransactionlistType2 bookedTransaction) {
        return ProviderTransactionDTO.builder()
                .externalId(bookedTransaction.getTransactionId())
                .dateTime(bookedTransaction.getValueDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(bookedTransaction.getValueDate()) : null)
                .amount(new BigDecimal(bookedTransaction.getTransactionAmount().getAmount()).abs())
                .status(TransactionStatus.BOOKED)
                .type(detectTransactionType(bookedTransaction.getTransactionAmount().getAmount()))
                .description(createDescription(bookedTransaction.getRemittanceInformationUnstructured(), bookedTransaction.getRemittanceInformationStructured()))
                .category(YoltCategory.GENERAL)
                .extendedTransaction(mapToExtendedTransactionDTO(bookedTransaction))
                .build();
    }

    private String createDescription(String remittanceInformationUnstructured,
                                     RemittanceInformationStructuredReadaccounttransactionlistType3 remittanceInformationStructured) {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(remittanceInformationUnstructured);

        if (remittanceInformationStructured != null) {
            joiner.add(remittanceInformationStructured.getReference())
                    .add(remittanceInformationStructured.getReferenceIssuer())
                    .add(remittanceInformationStructured.getReferenceType());
        }
        return joiner.toString();
    }

    private String createRemittanceInformationStructured(RemittanceInformationStructuredReadaccounttransactionlistType3 remittanceInformationStructured) {
        StringJoiner joiner = new StringJoiner(" ");
        if (remittanceInformationStructured != null) {
            joiner.add(remittanceInformationStructured.getReference())
                    .add(remittanceInformationStructured.getReferenceIssuer())
                    .add(remittanceInformationStructured.getReferenceType());
        }
        return joiner.toString();
    }

    private ExtendedTransactionDTO mapToExtendedTransactionDTO(TransactionsReadaccounttransactionlistType1BookedReadaccounttransactionlistType2 bookedTransaction) {
        return ExtendedTransactionDTO.builder()
                .valueDate(bookedTransaction.getValueDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(bookedTransaction.getValueDate()) : null)
                .bookingDate(bookedTransaction.getBookingDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(bookedTransaction.getBookingDate()) : null)
                .status(TransactionStatus.BOOKED)
                .transactionAmount(mapToTransactionAmount(bookedTransaction.getTransactionAmount()))
                .entryReference(bookedTransaction.getEntryReference())
                .creditorAccount(bookedTransaction.getCreditorAccount() != null ?
                        mapToAccountReference(bookedTransaction.getCreditorAccount().getIban()) : null)
                .creditorName(bookedTransaction.getCreditorName())
                .debtorAccount(bookedTransaction.getDebtorAccount() != null ?
                        mapToAccountReference(bookedTransaction.getDebtorAccount().getIban()) : null)
                .debtorName(bookedTransaction.getDebtorName())
                .remittanceInformationUnstructured(bookedTransaction.getRemittanceInformationUnstructured())
                .remittanceInformationStructured(createRemittanceInformationStructured(bookedTransaction.getRemittanceInformationStructured()))
                .build();
    }

    private ProviderTransactionDTO mapToProviderTransactionDTO(TransactionsReadaccounttransactionlistType1PendingReadaccounttransactionlistType2 pendingTransaction) {
        return ProviderTransactionDTO.builder()
                .externalId(pendingTransaction.getTransactionId())
                .dateTime(pendingTransaction.getValueDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(pendingTransaction.getValueDate()) : null)
                .amount(new BigDecimal(pendingTransaction.getTransactionAmount().getAmount()).abs())
                .status(TransactionStatus.PENDING)
                .type(detectTransactionType(pendingTransaction.getTransactionAmount().getAmount()))
                .description(pendingTransaction.getRemittanceInformationUnstructured() + " " + pendingTransaction.getRemittanceInformationStructured())
                .category(YoltCategory.GENERAL)
                .extendedTransaction(mapToExtendedTransactionDTO(pendingTransaction))
                .build();
    }

    private ProviderTransactionType detectTransactionType(String amount) {
        return new BigDecimal(amount).compareTo(BigDecimal.ZERO) > 0 ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT;
    }

    private ExtendedTransactionDTO mapToExtendedTransactionDTO(TransactionsReadaccounttransactionlistType1PendingReadaccounttransactionlistType2 pendingTransaction) {
        return ExtendedTransactionDTO.builder()
                .valueDate(pendingTransaction.getValueDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(pendingTransaction.getValueDate()) : null)
                .bookingDate(pendingTransaction.getBookingDate() != null ?
                        CbiGlobeDateUtil.dateToZonedDateTime(pendingTransaction.getBookingDate()) : null)
                .status(TransactionStatus.PENDING)
                .transactionAmount(mapToTransactionAmount(pendingTransaction.getTransactionAmount()))
                .entryReference(pendingTransaction.getEntryReference())
                .creditorAccount(pendingTransaction.getCreditorAccount() != null ?
                        mapToAccountReference(pendingTransaction.getCreditorAccount().getIban()) : null)
                .creditorName(pendingTransaction.getCreditorName())
                .debtorAccount(pendingTransaction.getDebtorAccount() != null ?
                        mapToAccountReference(pendingTransaction.getDebtorAccount().getIban()) : null)
                .debtorName(pendingTransaction.getDebtorName())
                .remittanceInformationUnstructured(pendingTransaction.getRemittanceInformationUnstructured())
                .remittanceInformationStructured(pendingTransaction.getRemittanceInformationStructured())
                .build();
    }

    private BalanceAmountDTO mapToTransactionAmount(TransactionAmountReadaccounttransactionlistType3 amountDetails) {
        CurrencyCode currency = StringUtils.isBlank(amountDetails.getCurrency()) ? CurrencyCode.EUR : currencyCodeMapper.toCurrencyCode(amountDetails.getCurrency());
        return BalanceAmountDTO.builder()
                .currency(currency)
                .amount(new BigDecimal(amountDetails.getAmount()))
                .build();
    }

    private AccountReferenceDTO mapToAccountReference(@NonNull String creditorOrDebtorIban) {
        return AccountReferenceDTO.builder()
                .type(AccountReferenceType.IBAN)
                .value(creditorOrDebtorIban.replace(" ", ""))
                .build();
    }
}
