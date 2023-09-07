package com.yolt.providers.axabanque.common.fetchdata.mapper;

import com.yolt.providers.axabanque.common.model.external.Account;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.providerdomain.AccountType;

public class DefaultAccountTypeMapper implements AccountTypeMapper {
    @Override
    public AccountType mapToAccountType(Account account) {
        return AccountType.CURRENT_ACCOUNT;
    }

    @Override
    public ExternalCashAccountType mapToExternalCashAccountType(Account account) {
        return ExternalCashAccountType.CURRENT;
    }
}
