package com.yolt.providers.monorepogroup.cecgroup.common.mapper;

import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.Transaction;
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
import java.util.Objects;

import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

public class CecGroupTransactionMapperV1 implements CecGroupTransactionMapper {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Bucharest");

    @Override
    public ProviderTransactionDTO mapToProviderTransaction(Transaction transaction, TransactionStatus transactionStatus) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(mapToZonedDateTime(TransactionStatus.BOOKED.equals(transactionStatus) ?
                        transaction.getBookingDate() : transaction.getValueDate()))
                .type(mapToProviderTransactionType(transaction))
                .category(YoltCategory.GENERAL)
                .amount(transaction.getAmount().abs())
                .description(defaultIfEmpty(transaction.getRemittanceInformationUnstructured(), "N/A"))
                .status(transactionStatus)
                .extendedTransaction(mapExtendedTransactionDTO(transaction, transactionStatus))
                .build();
    }


    private static ProviderTransactionType mapToProviderTransactionType(Transaction transaction) {
        return transaction.getAmount().compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT;
    }

    private ExtendedTransactionDTO mapExtendedTransactionDTO(Transaction transaction,
                                                             TransactionStatus status) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bookingDate(mapToZonedDateTime(TransactionStatus.BOOKED.equals(status) ?
                        transaction.getBookingDate() : transaction.getValueDate()))
                .valueDate(mapToZonedDateTime(transaction.getValueDate()))
                .status(status)
                .transactionAmount(mapToBalanceAmountDTO(transaction))
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

    private ZonedDateTime mapToZonedDateTime(LocalDate date) {
        return date.atStartOfDay(ZONE_ID);
    }

    private BalanceAmountDTO mapToBalanceAmountDTO(Transaction transaction) {
        return BalanceAmountDTO.builder()
                .amount(transaction.getAmount())
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
