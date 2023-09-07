package com.yolt.providers.openbanking.ais.tidegroup.common.service.ais;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountTypeMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import nl.ing.lovebird.providerdomain.AccountType;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code.EMONEY;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;

public class TideAccountTypeMapper extends DefaultAccountTypeMapper {

    @Override
    public AccountType apply(OBExternalAccountSubType1Code subtype) {
        if (EMONEY.equals(subtype)) {
            return CURRENT_ACCOUNT;
        }

        return super.apply(subtype);
    }
}
