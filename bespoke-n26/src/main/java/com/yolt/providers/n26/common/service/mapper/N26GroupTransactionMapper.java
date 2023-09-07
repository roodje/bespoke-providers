package com.yolt.providers.n26.common.service.mapper;

import com.yolt.providers.n26.common.dto.ais.fetchdata.Transaction;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.yolt.providers.n26.common.util.N26GroupDateUtil.getNullableZonedDateTime;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;

@AllArgsConstructor
public class N26GroupTransactionMapper {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public List<ProviderTransactionDTO> mapProviderTransactionsDTO(List<Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> mapProviderTransactionDTO(transaction))
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO mapProviderTransactionDTO(Transaction transaction) {
        ProviderTransactionType transactionType = toProviderTransactionType(transaction.getDecimalAmount());
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(getNullableZonedDateTime(transaction.getBookingDate().isEmpty() ? transaction.getValueDate() : transaction.getBookingDate(), DATE_FORMAT))
                .type(transactionType)
                .category(YoltCategory.GENERAL)
                .amount(transaction.getDecimalAmount().abs())
                .description(getTransactionDescription(transaction))
                .status(BOOKED)
                .extendedTransaction(mapExtendedTransactionDTO(transaction))
                .build();
    }

    private static ProviderTransactionType toProviderTransactionType(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0
                ? ProviderTransactionType.CREDIT
                : ProviderTransactionType.DEBIT;
    }

    private ExtendedTransactionDTO mapExtendedTransactionDTO(Transaction transaction) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .bookingDate(getNullableZonedDateTime(transaction.getBookingDate(), DATE_FORMAT))
                .valueDate(getNullableZonedDateTime(transaction.getValueDate(), DATE_FORMAT))
                .status(BOOKED)
                .transactionAmount(mapBalanceAmountDTO(transaction))
                .remittanceInformationUnstructured(getRemittanceInformationUnstructured(transaction))
                .transactionIdGenerated(true)
                .creditorId(transaction.getCreditorId())
                .mandateId(transaction.getMandateId());

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

    private BalanceAmountDTO mapBalanceAmountDTO(Transaction transaction) {
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

    private String getTransactionDescription(final Transaction transaction) {
        // All fields are optional according to RDD data. We just use a `N/A` for now.
        if (!StringUtils.isEmpty(transaction.getDetails())) {
            return transaction.getDetails();
        } else if (StringUtils.hasText(transaction.getRemittanceInformationUnstructured())) {
            return transaction.getRemittanceInformationUnstructured();
        } else if (!CollectionUtils.isEmpty(transaction.getRemittanceInformationUnstructuredArray())) {
            return transaction.getRemittanceInformationUnstructuredArray().get(0);
        } else if (!StringUtils.isEmpty(transaction.getCreditorName())) {
            return transaction.getCreditorName();
        } else if (!StringUtils.isEmpty(transaction.getDebtorName())) {
            return transaction.getDebtorName();
        } else {
            return "N/A";
        }
    }

    private String getRemittanceInformationUnstructured(Transaction transaction) {
        if (StringUtils.hasText(transaction.getRemittanceInformationUnstructured())) {
            return transaction.getRemittanceInformationUnstructured();
        } else if (!CollectionUtils.isEmpty(transaction.getRemittanceInformationUnstructuredArray())) {
            return String.join(" ", transaction.getRemittanceInformationUnstructuredArray());
        }
        return null;
    }
}
