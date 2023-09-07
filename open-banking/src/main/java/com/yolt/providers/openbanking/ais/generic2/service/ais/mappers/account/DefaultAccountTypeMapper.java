package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import nl.ing.lovebird.providerdomain.AccountType;

import java.util.function.Function;

import static nl.ing.lovebird.providerdomain.AccountType.*;

public class DefaultAccountTypeMapper implements Function<OBExternalAccountSubType1Code, AccountType> {
    @Override
    public AccountType apply(OBExternalAccountSubType1Code subtype) {
        if (subtype == null) {
            return null;
        }
        switch (subtype) {
            case CREDITCARD, CHARGECARD:
                return CREDIT_CARD;
            case CURRENTACCOUNT:
                return CURRENT_ACCOUNT;
            case PREPAIDCARD:
                return PREPAID_ACCOUNT;
            case SAVINGS:
                return SAVINGS_ACCOUNT;
            default:
                return null;
        }
    }
}
