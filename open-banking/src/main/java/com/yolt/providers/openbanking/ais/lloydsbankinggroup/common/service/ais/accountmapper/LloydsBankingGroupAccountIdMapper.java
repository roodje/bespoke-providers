package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.ais.accountmapper;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;

import java.util.function.Function;

public class LloydsBankingGroupAccountIdMapper implements Function<OBAccount6, String> {
    @Override
    public String apply(OBAccount6 account) {
        if (account == null || account.getAccount() == null || account.getAccount().get(0) == null) {
            return account.getAccountId();
        }
        return account.getAccount().get(0).getIdentification();
    }
}
