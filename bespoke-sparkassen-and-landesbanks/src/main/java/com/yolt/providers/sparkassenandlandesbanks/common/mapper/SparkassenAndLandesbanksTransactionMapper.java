package com.yolt.providers.sparkassenandlandesbanks.common.mapper;

import com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions.*;
import lombok.experimental.UtilityClass;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@UtilityClass
public class SparkassenAndLandesbanksTransactionMapper {

    private static final ZoneId BERLIN_ZONE_ID = ZoneId.of("Europe/Berlin");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProviderTransactionDTO toProviderTransactionDto(TransactionEntry transactionEntry) {
        return ProviderTransactionDTO.builder()
                .externalId(getEndToEndId(transactionEntry))
                .dateTime(getDateTime(transactionEntry))
                .type(toTransactionType(transactionEntry.getCreditDebitIndicator()))
                .category(YoltCategory.GENERAL)
                .amount(new BigDecimal(transactionEntry.getAmount().getValue()))
                .description(getRemittanceInformationUnstructured(transactionEntry))
                .status(toStatus(transactionEntry.getStatus()))
                .extendedTransaction(toExtendedTransactionDTO(transactionEntry))
                .build();
    }

    private ProviderTransactionType toTransactionType(String creditDebitIndicator) {
        switch (creditDebitIndicator) {
            case "CRDT":
                return ProviderTransactionType.CREDIT;
            case "DBIT":
                return ProviderTransactionType.DEBIT;
            default:
                return null;
        }
    }

    private TransactionStatus toStatus(String status) {
        switch (status) {
            case "BOOK":
                return TransactionStatus.BOOKED;
            case "PDNG":
                return TransactionStatus.PENDING;
            default:
                return null;
        }
    }

    private ZonedDateTime getDateTime(TransactionEntry sparkassenTransaction) {
        if (sparkassenTransaction.getBookingDate() != null) {
            return toDate(sparkassenTransaction.getBookingDate().getDate());
        }
        return toDate(sparkassenTransaction.getValueDate().getDate());
    }

    private ExtendedTransactionDTO toExtendedTransactionDTO(TransactionEntry transactionEntry) {
        return ExtendedTransactionDTO.builder()
                .bookingDate(transactionEntry.getBookingDate() != null ? toDate(transactionEntry.getBookingDate().getDate()) : null)
                .valueDate(transactionEntry.getValueDate() != null ? toDate(transactionEntry.getValueDate().getDate()) : null)
                .status(TransactionStatus.BOOKED)
                .transactionAmount(mapToTransactionAmount(transactionEntry))
                .remittanceInformationUnstructured(getRemittanceInformationUnstructured(transactionEntry))
                .creditorName(getCreditorName(transactionEntry))
                .creditorAccount(getCreditorAccountReference(transactionEntry))
                .debtorName(getDebtorName(transactionEntry))
                .debtorAccount(getDebtorAccountReference(transactionEntry))
                .transactionIdGenerated(true)
                .build();
    }

    private BalanceAmountDTO mapToTransactionAmount(TransactionEntry transactionEntry) {
        ProviderTransactionType providerTransactionType = toTransactionType(transactionEntry.getCreditDebitIndicator());
        BigDecimal amount = providerTransactionType == ProviderTransactionType.CREDIT ? new BigDecimal(transactionEntry.getAmount().getValue()) : new BigDecimal(transactionEntry.getAmount().getValue()).negate();
        return BalanceAmountDTO.builder()
                .currency(toCurrencyCode(transactionEntry.getAmount().getCurrency()))
                .amount(amount)
                .build();
    }

    private ZonedDateTime toDate(String transactionDate) {
        if (transactionDate == null) {
            return null;
        }
        return LocalDate.parse(transactionDate, DATE_TIME_FORMATTER).atStartOfDay(BERLIN_ZONE_ID);
    }

    private CurrencyCode toCurrencyCode(String currencyCode) {
        if (currencyCode == null) {
            return null;
        }
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    private AccountReferenceDTO getDebtorAccountReference(TransactionEntry transactionEntry) {
        return Optional.of(transactionEntry.getEntryDetails())
                .map(EntryDetails::getTransactionDetails)
                .map(TransactionDetails::getRelatedParties)
                .map(RelatedParties::getDebtorAccount)
                .map(DebtorAccount::getDebtorAccountIdentification)
                .map(DebtorAccountIdentification::getIban)
                .map(iban -> new AccountReferenceDTO(AccountReferenceType.IBAN, iban))
                .orElse(null);
    }

    private String getDebtorName(TransactionEntry transactionEntry) {
        return Optional.ofNullable(transactionEntry.getEntryDetails())
                .map(EntryDetails::getTransactionDetails)
                .map(TransactionDetails::getRelatedParties)
                .map(RelatedParties::getDebtor)
                .map(Debtor::getName)
                .orElse(null);
    }

    private AccountReferenceDTO getCreditorAccountReference(TransactionEntry transactionEntry) {
        return Optional.of(transactionEntry.getEntryDetails())
                .map(EntryDetails::getTransactionDetails)
                .map(TransactionDetails::getRelatedParties)
                .map(RelatedParties::getCreditorAccount)
                .map(CreditorAccount::getCreditorAccountIdentification)
                .map(CreditorAccountIdentification::getIban)
                .map(iban -> new AccountReferenceDTO(AccountReferenceType.IBAN, iban))
                .orElse(null);
    }

    private String getCreditorName(TransactionEntry transactionEntry) {
        return Optional.ofNullable(transactionEntry.getEntryDetails())
                .map(EntryDetails::getTransactionDetails)
                .map(TransactionDetails::getRelatedParties)
                .map(RelatedParties::getCreditor)
                .map(Creditor::getName)
                .orElse(null);
    }

    private String getRemittanceInformationUnstructured(TransactionEntry transactionEntry) {
        return Optional.ofNullable(transactionEntry.getEntryDetails())
                .map(EntryDetails::getTransactionDetails)
                .map(TransactionDetails::getRemittanceInformation)
                .map(RemittanceInformation::getUnstructured)
                .orElse(" ");
    }

    private String getEndToEndId(TransactionEntry transactionEntry) {
        return Optional.ofNullable(transactionEntry.getEntryDetails())
                .map(EntryDetails::getTransactionDetails)
                .map(TransactionDetails::getReferences)
                .map(References::getEndToEndId)
                .orElse(null);
    }
}
