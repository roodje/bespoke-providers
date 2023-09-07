package com.yolt.providers.fabric.common.mapper;

import com.yolt.providers.fabric.common.model.Account;
import nl.ing.lovebird.providerdomain.AccountType;

public interface AccountTypeMapper {

    public AccountType mapToAccountType(Account account);

}
