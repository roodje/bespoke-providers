package com.yolt.providers.fabric.common.mapper;

import com.yolt.providers.fabric.common.model.Account;
import com.yolt.providers.fabric.common.model.Balance;
import com.yolt.providers.fabric.common.model.Transactions;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.util.List;

public interface AccountMapper {
    ProviderAccountDTO map(Account account, List<Balance> balances, List<Transactions> transactions);
}
