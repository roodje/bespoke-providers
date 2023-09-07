package com.yolt.providers.brdgroup.common.mapper;

import com.yolt.providers.brdgroup.common.dto.fetchdata.Account;
import com.yolt.providers.brdgroup.common.dto.fetchdata.Balance;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface AccountMapper {

    ProviderAccountDTO map(Account account,
                           String providerDisplayName,
                           List<Balance> balances,
                           List<ProviderTransactionDTO> transactions);
}
