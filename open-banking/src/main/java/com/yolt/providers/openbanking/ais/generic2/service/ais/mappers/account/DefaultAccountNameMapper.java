package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
public class DefaultAccountNameMapper implements AccountNameMapper {
    private final Function<OBAccount6, String> accountNameFallback;

    @Override
    public String getAccountName(OBAccount6 account, OBAccount4Account mainAccountReference, List<OBAccount4Account> allAccountReferences) {
        if (account.getNickname() != null) {
            return account.getNickname();
        }
        String nameFromAccount = extractName(mainAccountReference);
        if (nameFromAccount != null) {
            return nameFromAccount;
        }
        if (allAccountReferences != null) {
            for (OBAccount4Account supportingAccountReference : allAccountReferences) {
                nameFromAccount = extractName(supportingAccountReference);
                if (nameFromAccount != null) {
                    return nameFromAccount;
                }
            }
        }
        return accountNameFallback.apply(account);
    }

    private String extractName(final OBAccount4Account obCashAccount3) {
        if (obCashAccount3 == null) {
            return null;
        }
        return obCashAccount3.getName();
    }
}
