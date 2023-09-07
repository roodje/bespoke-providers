package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.TransactionStatusMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.function.Function;

@RequiredArgsConstructor
public class DefaultTransactionMapper implements Function<OBTransaction6, ProviderTransactionDTO> {

    private final Function<OBTransaction6, ExtendedTransactionDTO> extendedTransactionMapper;
    private final Function<String, ZonedDateTime> mapDateTimeFunction;
    private final TransactionStatusMapper transactionStatusMapper;
    private final Function<String, BigDecimal> amountParser;
    private final Function<OBCreditDebitCode1, ProviderTransactionType> transactionTypeMapper;


    @Override
    public ProviderTransactionDTO apply(OBTransaction6 transaction) {
        String amount = transaction.getAmount().getAmount();

        String transactionInformation = getTransactionInformation(transaction);
        String merchantName = transaction.getMerchantDetails() == null ? null : transaction.getMerchantDetails().getMerchantName();

        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .category(YoltCategory.GENERAL)
                .amount(amountParser.apply(amount))
                .description(transactionInformation)
                .status(transactionStatusMapper.mapToTransactionStatus(transaction.getStatus()))
                .type(transactionTypeMapper.apply(transaction.getCreditDebitIndicator()))
                .dateTime(mapDateTimeFunction.apply(transaction.getBookingDateTime()))
                .merchant(merchantName)
                .extendedTransaction(extendedTransactionMapper.apply(transaction))
                .build();
    }

    protected String getTransactionInformation(final OBTransaction6 transaction) {
        String transactionInformation = transaction.getTransactionInformation();
        if (transactionInformation == null) {
            transactionInformation = " ";
        }
        return transactionInformation;
    }
}
