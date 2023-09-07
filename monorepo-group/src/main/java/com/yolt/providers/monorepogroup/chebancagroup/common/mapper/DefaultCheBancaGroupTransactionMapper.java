package com.yolt.providers.monorepogroup.chebancagroup.common.mapper;

import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Transaction;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
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
public class DefaultCheBancaGroupTransactionMapper implements CheBancaGroupTransactionMapper {

    private final CheBancaGroupDateMapper dateMapper;

    @Override
    public ProviderTransactionDTO map(final Transaction transaction, final TransactionStatus status) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getIdMoneyTransfer())
                .dateTime(ObjectUtils.isNotEmpty(transaction.getDateAccountingCurrency()) ? dateMapper.getZonedDateTime(transaction.getDateAccountingCurrency()) : null)
                .type(transaction.getAmountTransactionsAmount().compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT)
                .category(YoltCategory.GENERAL)
                .amount(transaction.getAmountTransactionsAmount().abs())
                .description(StringUtils.defaultIfEmpty(transaction.getDescription(), "N/A"))
                .status(status)
                .extendedTransaction(mapToExtendedTransactionDTO(transaction, status))
                .build();
    }

    private ExtendedTransactionDTO mapToExtendedTransactionDTO(final Transaction transaction, final TransactionStatus status) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bookingDate(ObjectUtils.isNotEmpty(transaction.getDateAccountingCurrency()) ? dateMapper.getZonedDateTime(transaction.getDateAccountingCurrency()) : null)
                .valueDate(ObjectUtils.isNotEmpty(transaction.getDateAccountingCurrency()) ? dateMapper.getZonedDateTime(transaction.getDateAccountingCurrency()) : null)
                .status(status)
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(transaction.getAmountTransactionsAmount())
                        .currency(mapToCurrencyCode(transaction.getAmountTransactionsCurrency()))
                        .build())
                .remittanceInformationUnstructured(transaction.getDescription())
                .transactionIdGenerated(true);
        return builder.build();
    }

    private CurrencyCode mapToCurrencyCode(final String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
