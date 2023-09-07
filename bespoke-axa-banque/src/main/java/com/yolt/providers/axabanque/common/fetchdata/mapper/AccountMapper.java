package com.yolt.providers.axabanque.common.fetchdata.mapper;

import com.yolt.providers.axabanque.common.model.external.Account;
import com.yolt.providers.axabanque.common.model.external.Balance;
import com.yolt.providers.axabanque.common.model.external.Transactions;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.util.List;

public interface AccountMapper {
    ProviderAccountDTO map(Account account, List<Balance> balances, List<Transactions> transactions);
}
