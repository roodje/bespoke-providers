package com.yolt.providers.openbanking.ais.tescobank.service.ais.mappers.account;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;

@RequiredArgsConstructor
public class TescoBankAccountIdMapper implements Function<OBAccount6, String> {

    private static final String OB_3_0_0_SCHEME_PREFIX = "UK.OBIE.";
    private static final String PAN_SCHEMA_NAME = OB_3_0_0_SCHEME_PREFIX + "PAN";
    private final DefaultAccountTypeMapper defaultAccountTypeMapper;
    private final DefaultSupportedSchemeAccountFilter defaultSupportedSchemeAccountFilter;

    @Override
    public String apply(OBAccount6 account) {
        if (CREDIT_CARD.equals(defaultAccountTypeMapper.apply(account.getAccountSubType()))
                && defaultSupportedSchemeAccountFilter.findFirstAccountWhereSchemeIsSupported(account.getAccount()) == null) {
            return account.getAccount().stream()
                    .filter(subAccount -> PAN_SCHEMA_NAME.equalsIgnoreCase(subAccount.getSchemeName()))
                    .findAny()
                    .map(OBAccount4Account::getIdentification)
                    .map(this::removeMask)
                    .orElse(account.getAccountId());
        }
        return account.getAccountId();
    }

    private String removeMask(String maskedPAN) {
        return maskedPAN.substring(maskedPAN.lastIndexOf('*') + 1);
    }
}
