package com.yolt.providers.stet.generic.mapper.account;

import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface AccountMapper {

    List<StetBalanceType> getPreferredCurrentBalanceTypes();

    List<StetBalanceType> getPreferredAvailableBalanceTypes();

    List<StetBalanceType> getPreferredCardBalanceType();

    ProviderAccountDTO mapToProviderAccountDTO(StetAccountDTO account,
                                               List<StetBalanceDTO> balances,
                                               List<ProviderTransactionDTO> providerTransactions);

}
