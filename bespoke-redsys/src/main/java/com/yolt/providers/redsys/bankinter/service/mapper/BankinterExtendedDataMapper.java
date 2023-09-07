package com.yolt.providers.redsys.bankinter.service.mapper;

import com.yolt.providers.redsys.common.dto.Transaction;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapper;
import com.yolt.providers.redsys.common.service.mapper.RedsysExtendedDataMapperV2;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BankinterExtendedDataMapper extends RedsysExtendedDataMapperV2 {

    public BankinterExtendedDataMapper(CurrencyCodeMapper currencyCodeMapper, ZoneId zoneId) {
        super(currencyCodeMapper, zoneId);
    }

    @Override
    public ExtendedTransactionDTO toExtendedTransactionDTO(final Transaction transaction,
                                                           final TransactionStatus transactionStatus) {

        return super.toExtendedTransactionDTO(transaction, transactionStatus)
                .toBuilder()
                .remittanceInformationUnstructured(formatRemittanceInformation(transaction.getRemittanceInformationUnstructured()))
                .build();
    }

    @Override
    protected String formatRemittanceInformation(String remittanceInformationUnstructured) {
        String withRemovedPrefixes = remittanceInformationUnstructured.replace("/TXT/", "")
                .replace("D|", "").replace("H|", "");
        return withRemovedPrefixes.split("\\|")[0];
    }
}
