package com.yolt.providers.openbanking.ais.monzogroup.common.service.ais.transcationmapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.TransactionStatusMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.function.Function;


public class MonzoTransactionMapperV3 extends DefaultTransactionMapper {

    private static final String POT_SCHEME = "uk_retail_pot";
    private static final String MONZO_POT_DESCRIPTION = "Monzo pot";

    public MonzoTransactionMapperV3(Function<OBTransaction6, ExtendedTransactionDTO> extendedTransactionMapper,
                                    Function<String, ZonedDateTime> mapDateTimeFunction,
                                    TransactionStatusMapper transactionStatusMapper,
                                    Function<String, BigDecimal> amountParser,
                                    Function<OBCreditDebitCode1, ProviderTransactionType> transactionTypeMapper) {
        super(extendedTransactionMapper, mapDateTimeFunction, transactionStatusMapper, amountParser, transactionTypeMapper);
    }


    @Override
    protected String getTransactionInformation(OBTransaction6 transaction) {
        String description = super.getTransactionInformation(transaction);
        if (POT_SCHEME.equals(transaction.getProprietaryBankTransactionCode().getCode())) {
            description = MONZO_POT_DESCRIPTION;
        }
        return description;
    }
}
