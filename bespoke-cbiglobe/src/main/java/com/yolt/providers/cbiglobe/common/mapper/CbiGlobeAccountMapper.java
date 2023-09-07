package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadAccountBalanceResponseTypeBalances;
import com.yolt.providers.cbiglobe.dto.ReadAccountListResponseTypeAccounts;
import com.yolt.providers.cbiglobe.dto.TransactionsReadaccounttransactionlistType1;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.util.List;

public interface CbiGlobeAccountMapper {

    ProviderAccountDTO mapToProviderAccountDTO(ReadAccountListResponseTypeAccounts account);

    ProviderAccountDTO updateProviderAccountDTO(ProviderAccountDTO account,
                                                List<ReadAccountBalanceResponseTypeBalances> balances,
                                                List<TransactionsReadaccounttransactionlistType1> transactions);
}
