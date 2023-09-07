package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.mappers.transaction;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.TransactionStatusMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

public class BarclaysGroupTransactionMapper extends DefaultTransactionMapper {

    private static final ZoneId BARCLAYS_ZONE_ID = ZoneId.of("Europe/London");

    private final Function<OBTransaction6, ExtendedTransactionDTO> extendedTransactionMapper;
    private final TransactionStatusMapper transactionStatusMapper;
    private final Function<String, BigDecimal> amountParser;
    private final Function<OBCreditDebitCode1, ProviderTransactionType> transactionTypeMapper;

    public BarclaysGroupTransactionMapper(final Function<OBTransaction6, ExtendedTransactionDTO> extendedTransactionMapper,
                                          final Function<String, ZonedDateTime> mapDateTimeFunction,
                                          final TransactionStatusMapper transactionStatusMapper,
                                          final Function<String, BigDecimal> amountParser,
                                          final Function<OBCreditDebitCode1, ProviderTransactionType> transactionTypeMapper) {
        super(extendedTransactionMapper, mapDateTimeFunction, transactionStatusMapper, amountParser, transactionTypeMapper);
        this.extendedTransactionMapper = extendedTransactionMapper;
        this.transactionStatusMapper = transactionStatusMapper;
        this.amountParser = amountParser;
        this.transactionTypeMapper = transactionTypeMapper;
    }


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
                .dateTime(mapDateTime(transaction))
                .merchant(merchantName)
                .extendedTransaction(extendedTransactionMapper.apply(transaction))
                .build();
    }

    private ZonedDateTime mapDateTime(OBTransaction6 transaction) {
        if (transaction.getBookingDateTime() != null) {
            return ZonedDateTime.parse(transaction.getBookingDateTime()).withZoneSameInstant(BARCLAYS_ZONE_ID);
        }
        if (transaction.getValueDateTime() != null) {
            return ZonedDateTime.parse(transaction.getValueDateTime()).withZoneSameInstant(BARCLAYS_ZONE_ID);
        }
        return null;
    }
}
