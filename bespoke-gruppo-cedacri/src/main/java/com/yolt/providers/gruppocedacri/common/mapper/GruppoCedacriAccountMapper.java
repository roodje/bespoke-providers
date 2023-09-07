package com.yolt.providers.gruppocedacri.common.mapper;

import com.yolt.providers.gruppocedacri.common.dto.fetchdata.Account;
import com.yolt.providers.gruppocedacri.common.dto.fetchdata.Balance;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface GruppoCedacriAccountMapper {

    ProviderAccountDTO map(Account account,
                           String providerDisplayName,
                           List<Balance> balances,
                           List<ProviderTransactionDTO> transactions);
}
