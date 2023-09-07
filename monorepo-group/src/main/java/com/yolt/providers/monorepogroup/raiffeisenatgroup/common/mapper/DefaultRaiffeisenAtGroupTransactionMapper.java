package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Transaction;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupTransactionMapper implements RaiffeisenAtGroupTransactionMapper {

    private final RaiffeisenAtGroupDateMapper dateMapper;

    @Override
    public ProviderTransactionDTO map(Transaction transaction, TransactionStatus status) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(ObjectUtils.isNotEmpty(transaction.getBookingDate()) ? dateMapper.getZonedDateTime(transaction.getBookingDate()) : null)
                .type(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT)
                .category(YoltCategory.GENERAL)
                .amount(transaction.getAmount().abs())
                .description(StringUtils.defaultIfEmpty(transaction.getRemittanceInformationUnstructured(), "N/A"))
                .status(status)
                .extendedTransaction(mapToExtendedTransactionDTO(transaction, status))
                .build();
    }

    private ExtendedTransactionDTO mapToExtendedTransactionDTO(Transaction transaction, TransactionStatus status) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bookingDate(ObjectUtils.isNotEmpty(transaction.getBookingDate()) ? dateMapper.getZonedDateTime(transaction.getBookingDate()) : null)
                .valueDate(ObjectUtils.isNotEmpty(transaction.getValueDate()) ? dateMapper.getZonedDateTime(transaction.getValueDate()) : null)
                .status(status)
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(transaction.getAmount())
                        .currency(mapToCurrencyCode(transaction.getCurrency()))
                        .build())
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .transactionIdGenerated(true);

        if (ObjectUtils.isNotEmpty(transaction.getCreditorName())) {
            builder.creditorName(transaction.getCreditorName())
                    .creditorAccount(mapAccountReferenceDTO(transaction.getCreditorIban()));
        }
        if (ObjectUtils.isNotEmpty(transaction.getDebtorName())) {
            builder.debtorName(transaction.getDebtorName())
                    .debtorAccount(mapAccountReferenceDTO(transaction.getDebtorIban()));
        }

        return builder.build();
    }

    private CurrencyCode mapToCurrencyCode(String currencyCode) {
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
