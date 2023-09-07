package com.yolt.providers.volksbank.common.service.mapper;

import com.yolt.providers.volksbank.dto.v1_1.AccountDetails;
import com.yolt.providers.volksbank.dto.v1_1.BalanceItem;
import com.yolt.providers.volksbank.dto.v1_1.TransactionItem;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface VolksbankDataMapperService {

    ProviderAccountDTO mapToProviderAccountDTO(AccountDetails account,
                                               BalanceItem accountBalance,
                                               List<ProviderTransactionDTO> transactionsConverted,
                                               String providerName);

    ProviderTransactionDTO mapToProviderTransactionDTO(TransactionItem transaction);
}
