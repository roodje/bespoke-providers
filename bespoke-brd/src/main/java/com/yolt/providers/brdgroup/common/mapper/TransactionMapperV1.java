package com.yolt.providers.brdgroup.common.mapper;

import com.yolt.providers.brdgroup.common.dto.fetchdata.Transaction;
import com.yolt.providers.brdgroup.common.util.BrdGroupDateConverter;
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

import java.math.BigDecimal;
import java.util.Objects;

import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@RequiredArgsConstructor
public class TransactionMapperV1 implements TransactionMapper {

    private final BrdGroupDateConverter dateConverter;

    @Override
    public ProviderTransactionDTO map(Transaction transaction, TransactionStatus status) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getEndToEndId())
                .dateTime(dateConverter.determineTransactionDateTime(transaction.getBookingDate(), transaction.getValueDate()))
                .type(mapToProviderTransactionType(transaction))
                .category(YoltCategory.GENERAL)
                .amount(transaction.getDecimalAmount().abs())
                .description(defaultIfEmpty(transaction.getRemittanceInformationUnstructured(), "N/A"))
                .status(status)
                .extendedTransaction(mapExtendedTransactionDTO(transaction, status))
                .build();
    }

    private static ProviderTransactionType mapToProviderTransactionType(Transaction transaction) {
        return new BigDecimal(transaction.getAmount()).compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT;
    }

    private ExtendedTransactionDTO mapExtendedTransactionDTO(Transaction transaction,
                                                             TransactionStatus status) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bookingDate(dateConverter.getNullableZonedDateTime(transaction.getBookingDate()))
                .valueDate(dateConverter.getNullableZonedDateTime(transaction.getValueDate()))
                .status(status)
                .transactionAmount(mapBalanceAmountDTO(transaction))
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

    private BalanceAmountDTO mapBalanceAmountDTO(Transaction transaction) {
        return BalanceAmountDTO.builder()
                .amount(transaction.getDecimalAmount())
                .currency(mapToCurrencyCode(transaction.getCurrency()))
                .build();
    }

    private static CurrencyCode mapToCurrencyCode(String currencyCode) {
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
