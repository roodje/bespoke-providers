package com.yolt.providers.consorsbankgroup.common.ais.mapper;

import com.yolt.providers.consorsbankgroup.consorsbank.ConsorsbankBeanConfig;
import com.yolt.providers.consorsbankgroup.dto.AccountReference;
import com.yolt.providers.consorsbankgroup.dto.TransactionDetails;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;

import java.math.BigDecimal;

import static com.yolt.providers.consorsbankgroup.consorsbank.ConsorsbankBeanConfig.ZONE_ID;

public class DefaultExtendedModelTransactionMapper {

    public ExtendedTransactionDTO mapTransaction(final TransactionDetails transactionDetails, final TransactionStatus status) {
        return ExtendedTransactionDTO.builder()
                .bookingDate(transactionDetails.getBookingDate() == null ? transactionDetails.getValueDate().atStartOfDay(ZONE_ID) : transactionDetails.getBookingDate().atStartOfDay(ZONE_ID))
                .valueDate(transactionDetails.getValueDate().atStartOfDay(ZONE_ID))
                .creditorAccount(mapToAccountReference(transactionDetails.getCreditorAccount()))
                .creditorName(transactionDetails.getCreditorName())
                .debtorAccount(mapToAccountReference(transactionDetails.getDebtorAccount()))
                .debtorName(transactionDetails.getDebtorName())
                .endToEndId(transactionDetails.getEndToEndId())
                .mandateId(transactionDetails.getMandateId())
                .proprietaryBankTransactionCode(transactionDetails.getProprietaryBankTransactionCode())
                .transactionAmount(mapTransactionAmount(transactionDetails))
                .status(status)
                .transactionIdGenerated(true)
                .build();
    }

    private BalanceAmountDTO mapTransactionAmount(final TransactionDetails transactionDetails) {
        return BalanceAmountDTO.builder()
                .amount(new BigDecimal(transactionDetails.getTransactionAmount().getAmount()))
                .currency(CurrencyCode.valueOf(transactionDetails.getTransactionAmount().getCurrency()))
                .build();
    }

    private AccountReferenceDTO mapToAccountReference(final AccountReference accountReference) {
        if (accountReference != null && accountReference.getIban() != null) {
            return new AccountReferenceDTO(AccountReferenceType.IBAN, accountReference.getIban());
        }
        return null;
    }
}
