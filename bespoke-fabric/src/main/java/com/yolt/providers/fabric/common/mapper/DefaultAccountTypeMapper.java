package com.yolt.providers.fabric.common.mapper;

import com.yolt.providers.fabric.common.model.Account;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.providerdomain.AccountType;

public class DefaultAccountTypeMapper implements AccountTypeMapper {
    @Override
    public AccountType mapToAccountType(Account account) {
        return AccountType.CURRENT_ACCOUNT;
    }

}
