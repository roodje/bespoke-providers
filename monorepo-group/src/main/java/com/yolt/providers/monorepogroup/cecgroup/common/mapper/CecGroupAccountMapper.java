package com.yolt.providers.monorepogroup.cecgroup.common.mapper;

import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.Account;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface CecGroupAccountMapper {
    ProviderAccountDTO mapToProviderAccount(Account account,
                                            List<ProviderTransactionDTO> transactions,
                                            String providerDisplayName);
}
