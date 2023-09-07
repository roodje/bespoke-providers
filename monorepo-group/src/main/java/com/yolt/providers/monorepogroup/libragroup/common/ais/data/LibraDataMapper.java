package com.yolt.providers.monorepogroup.libragroup.common.ais.data;

import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Account;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Balances;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Transactions;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface LibraDataMapper {
    List<ProviderTransactionDTO> mapTransactions(Transactions transactions);

    ProviderAccountDTO mapAccountData(Account account, Balances balances, List<ProviderTransactionDTO> mappedTransactions);
}
