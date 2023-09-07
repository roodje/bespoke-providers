package com.yolt.providers.axabanque.common.fetchdata.mapper;

import com.yolt.providers.axabanque.common.model.external.Account;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.providerdomain.AccountType;

public interface AccountTypeMapper {

    public AccountType mapToAccountType(Account account);

    public ExternalCashAccountType mapToExternalCashAccountType(Account account);
}
