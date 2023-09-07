package com.yolt.providers.bancacomercialaromana.common.mapper;

import com.yolt.providers.bancacomercialaromana.common.model.fetchdata.Transaction;
import lombok.AllArgsConstructor;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.yolt.providers.bancacomercialaromana.common.util.BcrGroupDateUtil.getNullableZonedDateTime;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@AllArgsConstructor
public class BcrGroupTransactionMapper {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public List<ProviderTransactionDTO> mapProviderTransactionsDTO(List<Transaction> transactions, TransactionStatus status) {
        return transactions.stream()
                .map(transaction -> mapProviderTransactionDTO(transaction, status))
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO mapProviderTransactionDTO(Transaction transaction, TransactionStatus status) {
        ProviderTransactionType transactionType = toProviderTransactionType(transaction);
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(getNullableZonedDateTime(status.equals(BOOKED) ? transaction.getBookingDate() : transaction.getValueDate(), DATE_FORMAT))
                .type(transactionType)
                .category(YoltCategory.GENERAL)
                .amount(transaction.getDecimalAmount().abs())
                .description(defaultIfEmpty(transaction.getRemittanceInformationUnstructured(), transaction.getAdditionalInformation()))
                .status(status)
                .extendedTransaction(mapExtendedTransactionDTO(transaction, status, transactionType))
                .build();
    }

    private static ProviderTransactionType toProviderTransactionType(Transaction transaction) {
        return transaction.getDecimalAmount().compareTo(BigDecimal.ZERO) > 0
                ? ProviderTransactionType.CREDIT
                : ProviderTransactionType.DEBIT;
    }

    private ExtendedTransactionDTO mapExtendedTransactionDTO(Transaction transaction,
                                                             TransactionStatus status,
                                                             ProviderTransactionType transactionType) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .bookingDate(getNullableZonedDateTime(transaction.getBookingDate(), DATE_FORMAT))
                .valueDate(getNullableZonedDateTime(transaction.getValueDate(), DATE_FORMAT))
                .status(status)
                .transactionAmount(mapBalanceAmountDTO(transaction, transactionType))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .transactionIdGenerated(true);

        if (Objects.nonNull(transaction.getCreditorName())) {
            builder.creditorName(transaction.getCreditorName())
                    .creditorAccount(mapAccountReferenceDTO(transaction.getCreditorIban()));
        }
        if (Objects.nonNull(transaction.getDebtorName())) {
            builder.debtorName(transaction.getDebtorName())
                    .debtorAccount(mapAccountReferenceDTO(transaction.getDebtorIban()));
        }
        return builder.build();
    }

    private BalanceAmountDTO mapBalanceAmountDTO(Transaction transaction, ProviderTransactionType transactionType) {
        return BalanceAmountDTO.builder()
                .amount(transaction.getDecimalAmount())
                .currency(toCurrencyCode(transaction.getCurrency()))
                .build();
    }

    private static CurrencyCode toCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private AccountReferenceDTO mapAccountReferenceDTO(String iban) {
        return AccountReferenceDTO.builder()
                .type(AccountReferenceType.IBAN)
                .value(iban)
                .build();
    }
}
