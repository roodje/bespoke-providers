package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.dto.GetTransactionsResponseTransactionsBooked;
import com.yolt.providers.argentagroup.dto.GetTransactionsResponseTransactionsCreditorAccount;
import com.yolt.providers.argentagroup.dto.GetTransactionsResponseTransactionsDebtorAccount;
import com.yolt.providers.common.exception.MissingDataException;
import lombok.RequiredArgsConstructor;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RequiredArgsConstructor
public class TransactionsMapper {

    private final ZoneId transactionsDateTimeZone;

    public ProviderTransactionDTO mapBookedTransaction(final GetTransactionsResponseTransactionsBooked transaction) {
        return mapTransaction(transaction, TransactionStatus.BOOKED);
    }

    public ProviderTransactionDTO mapPendingTransaction(final GetTransactionsResponseTransactionsBooked transaction) {
        return mapTransaction(transaction, TransactionStatus.PENDING);
    }

    private ProviderTransactionDTO mapTransaction(final GetTransactionsResponseTransactionsBooked transaction,
                                                  final TransactionStatus status) {
        ZonedDateTime dateTime = Optional.of(transaction)
                .map(t -> mapDateField(t.getBookingDate()))
                .or(() -> Optional.ofNullable(mapDateField(transaction.getTransactionDate())))
                .orElseThrow(() -> new MissingDataException("Transaction dateTime fields are empty"));

        BigDecimal amountWithSign = new BigDecimal(transaction.getTransactionAmount().getAmount());
        ProviderTransactionType type = amountWithSign.signum() < 0 ? ProviderTransactionType.DEBIT : ProviderTransactionType.CREDIT;

        String description = Optional.of(transaction)
                .map(GetTransactionsResponseTransactionsBooked::getRemittanceInformationStructured)
                .filter(StringUtils::isNotBlank)
                .or(() -> Optional.ofNullable(transaction.getRemittanceInformationUnstructured()))
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new MissingDataException("Transaction description is empty"));

        return ProviderTransactionDTO.builder()
                .externalId(transaction.getEntryReference())
                .dateTime(dateTime)
                .amount(amountWithSign.abs())
                .status(status)
                .type(type)
                .description(description)
                .category(YoltCategory.GENERAL)
                .extendedTransaction(
                        ExtendedTransactionDTO.builder()
                                .status(status)
                                .entryReference(transaction.getEntryReference())
                                .bookingDate(mapDateField(transaction.getBookingDate()))
                                .valueDate(mapDateField(transaction.getValueDate()))
                                .transactionAmount(new BalanceAmountDTO(
                                        CurrencyCode.valueOf(transaction.getTransactionAmount().getCurrency()),
                                        amountWithSign.abs()
                                ))
                                .creditorName(transaction.getCreditorName())
                                .debtorName(transaction.getDebtorName())
                                .creditorAccount(mapCreditorAccountReference(transaction.getCreditorAccount()))
                                .debtorAccount(mapDebtorAccountReference(transaction.getDebtorAccount()))
                                .remittanceInformationStructured(transaction.getRemittanceInformationStructured())
                                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode())
                                .build()
                )
                .build();
    }

    private ZonedDateTime mapDateField(final String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(transactionsDateTimeZone); //TODO C4PO-9187
    }

    private AccountReferenceDTO mapDebtorAccountReference(final GetTransactionsResponseTransactionsDebtorAccount debtorAccount) {
        if (debtorAccount == null) {
            return null;
        }
        if (StringUtils.isNotBlank(debtorAccount.getIban())) {
            return new AccountReferenceDTO(AccountReferenceType.IBAN, debtorAccount.getIban());
        } else if (StringUtils.isNotBlank(debtorAccount.getBban())) {
            return new AccountReferenceDTO(AccountReferenceType.BBAN, debtorAccount.getBban());
        } else if (StringUtils.isNotBlank(debtorAccount.getMaskedPan())) {
            return new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, debtorAccount.getMaskedPan());
        } else if (StringUtils.isNotBlank(debtorAccount.getPan())) {
            return new AccountReferenceDTO(AccountReferenceType.PAN, debtorAccount.getPan());
        } else if (StringUtils.isNotBlank(debtorAccount.getMsisdn())) {
            return new AccountReferenceDTO(AccountReferenceType.MSISDN, debtorAccount.getMsisdn());
        }
        return null;
    }

    private AccountReferenceDTO mapCreditorAccountReference(final GetTransactionsResponseTransactionsCreditorAccount creditorAccount) {
        if (creditorAccount == null) {
            return null;
        }
        if (StringUtils.isNotBlank(creditorAccount.getIban())) {
            return new AccountReferenceDTO(AccountReferenceType.IBAN, creditorAccount.getIban());
        } else if (StringUtils.isNotBlank(creditorAccount.getBban())) {
            return new AccountReferenceDTO(AccountReferenceType.BBAN, creditorAccount.getBban());
        } else if (StringUtils.isNotBlank(creditorAccount.getMaskedPan())) {
            return new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, creditorAccount.getMaskedPan());
        } else if (StringUtils.isNotBlank(creditorAccount.getPan())) {
            return new AccountReferenceDTO(AccountReferenceType.PAN, creditorAccount.getPan());
        } else if (StringUtils.isNotBlank(creditorAccount.getMsisdn())) {
            return new AccountReferenceDTO(AccountReferenceType.MSISDN, creditorAccount.getMsisdn());
        }
        return null;
    }
}
