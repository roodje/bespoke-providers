package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode1;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;

import java.util.function.Function;

public class DefaultTransactionTypeMapper implements Function<OBCreditDebitCode1, ProviderTransactionType> {

    @Override
    public ProviderTransactionType apply(OBCreditDebitCode1 creditorCode) {
        if (creditorCode == null) {
            return null;
        }
        switch (creditorCode) {
            case CREDIT:
                return ProviderTransactionType.CREDIT;
            case DEBIT:
                return ProviderTransactionType.DEBIT;
            default:
                return null;
        }
    }
}
