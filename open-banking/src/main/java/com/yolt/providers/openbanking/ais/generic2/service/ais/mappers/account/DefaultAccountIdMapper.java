package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;

import java.util.function.Function;

public class DefaultAccountIdMapper implements Function<OBAccount6, String> {
    @Override
    public String apply(OBAccount6 account) {
        return account.getAccountId();
    }
}
