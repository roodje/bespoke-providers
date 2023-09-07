package com.yolt.providers.kbcgroup.common.mapper;

import com.yolt.providers.kbcgroup.dto.AccountReference1;
import com.yolt.providers.kbcgroup.dto.Transaction1;
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
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@UtilityClass
public class KbcGroupTransactionMapper {

    private static final ZoneId BRUSSELS_ZONE_ID = ZoneId.of("Europe/Brussels");

    public ProviderTransactionDTO toProviderTransactionDto(Transaction1 kbcTransaction) {
        String amount = kbcTransaction.getTransactionAmount().getAmount();
        return ProviderTransactionDTO.builder()
                .externalId(kbcTransaction.getTransactionId())
                .dateTime(toDate(kbcTransaction.getBookingDate()))
                .type(deduceTransactionType(amount))
                .category(YoltCategory.GENERAL)
                .amount(new BigDecimal(amount).abs())
                .description(StringUtils.trimToEmpty(kbcTransaction.getRemittanceInformationUnstructured()))
                .status(TransactionStatus.BOOKED)
                .extendedTransaction(toExtendedTransactionDTO(kbcTransaction))
                .build();
    }

    private ProviderTransactionType deduceTransactionType(String amount) {
        return new BigDecimal(amount).compareTo(BigDecimal.ZERO) > 0 ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT;
    }

    private ExtendedTransactionDTO toExtendedTransactionDTO(Transaction1 transaction) {
        return ExtendedTransactionDTO.builder()
                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .bankTransactionCode(transaction.getBankTransactionCode())
                .bookingDate(toDate(transaction.getBookingDate()))
                .valueDate(toDate(transaction.getValueDate()))
                .status(TransactionStatus.BOOKED)
                .transactionAmount(mapToTransactionAmount(transaction))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .creditorName(transaction.getCreditorName())
                .creditorAccount(toAccountReference(transaction.getCreditorAccount()))
                .debtorName(transaction.getDebtorName())
                .debtorAccount(toAccountReference(transaction.getDebtorAccount()))
                .transactionIdGenerated(true)
                .build();
    }

    private AccountReferenceDTO toAccountReference(AccountReference1 accountReference) {
        if (accountReference == null) {
            return null;
        }
        return new AccountReferenceDTO(AccountReferenceType.IBAN, accountReference.getIban());
    }

    private BalanceAmountDTO mapToTransactionAmount(final Transaction1 transaction) {
        return BalanceAmountDTO.builder()
                .currency(toCurrencyCode(transaction.getTransactionAmount().getCurrency()))
                .amount(new BigDecimal(transaction.getTransactionAmount().getAmount()))
                .build();
    }

    private ZonedDateTime toDate(LocalDate transactionDate) {
        if (transactionDate == null) {
            return null;
        }
        return transactionDate.atStartOfDay(BRUSSELS_ZONE_ID);
    }

    private static CurrencyCode toCurrencyCode(String currencyCode) {
        if (currencyCode == null) {
            return null;
        }
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }
}
