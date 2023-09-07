package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadCardAccountBalancesResponseTypeBalances;
import com.yolt.providers.cbiglobe.dto.ReadCardAccountListResponseTypeCardAccounts;
import com.yolt.providers.cbiglobe.dto.ReadCardAccountTransactionListResponseTypeTransactions;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.util.List;

public interface CbiGlobeCardAccountMapper {

    ProviderAccountDTO mapToProviderAccountDTO(ReadCardAccountListResponseTypeCardAccounts account);

    ProviderAccountDTO updateProviderAccountDTO(ProviderAccountDTO account,
                                                List<ReadCardAccountBalancesResponseTypeBalances> balances,
                                                List<ReadCardAccountTransactionListResponseTypeTransactions> transactions);
}
