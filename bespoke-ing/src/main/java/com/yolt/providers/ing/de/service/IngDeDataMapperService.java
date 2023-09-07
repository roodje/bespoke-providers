package com.yolt.providers.ing.de.service;

import com.yolt.providers.ing.common.dto.Transactions.Transaction;
import com.yolt.providers.ing.common.service.IngDataMapperServiceV6;

import java.time.ZoneId;

public class IngDeDataMapperService extends IngDataMapperServiceV6 {

    private static final String REMITTANCE_INFORMATION = "remittanceinformation:";

    public IngDeDataMapperService(ZoneId zoneId) {
        super(zoneId);
    }

    @Override
    public String retrieveSanitizedTransactionDescription(Transaction transaction) {
        String unrefinedTransactionDescription = transaction.getDescription();
        if (unrefinedTransactionDescription != null) {
            int index = unrefinedTransactionDescription.indexOf(REMITTANCE_INFORMATION);
            if (index > -1) {
                String refinedTransactionDescription = unrefinedTransactionDescription.substring(index + REMITTANCE_INFORMATION.length());
                if (!refinedTransactionDescription.isBlank()) {
                    return LINE_BREAK_TAG_PATTERN.matcher(refinedTransactionDescription).replaceAll("\n");
                }
            } else if (!unrefinedTransactionDescription.isBlank()) {
                return LINE_BREAK_TAG_PATTERN.matcher(unrefinedTransactionDescription).replaceAll("\n");
            }
        }
        return ING_TRANSACTION_DESCRIPTION_NOT_AVAILABLE;
    }
}
