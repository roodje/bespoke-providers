package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.accountmapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.SupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.AccountType;

import java.util.function.Function;

import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;

@AllArgsConstructor
public class HsbcGroupAccountIdMapper implements Function<OBAccount6, String> {
    private static final String PAN_SCHEMA_NAME = "UK.OBIE.PAN";
    private final Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper;
    private final SupportedSchemeAccountFilter supportedSchemeAccountFilter;

    @Override
    public String apply(OBAccount6 account) {
        if (CREDIT_CARD.equals(accountTypeMapper.apply(account.getAccountSubType())) &&
                supportedSchemeAccountFilter.findFirstAccountWhereSchemeIsSupported(account.getAccount()) == null) {
            return account.getAccount().stream()
                    .filter(subAccount -> PAN_SCHEMA_NAME.equalsIgnoreCase(subAccount.getSchemeName()))
                    .findAny()
                    .map(OBAccount4Account::getIdentification)
                    .orElse(account.getAccountId());
        }
        return account.getAccountId();
    }
}
