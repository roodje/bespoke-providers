package com.yolt.providers.monorepogroup.chebancagroup.common.mapper;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Balances;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

public interface CheBancaGroupAccountMapper {

    ProviderAccountDTO.ProviderAccountDTOBuilder map(Account account, Balances balance) throws ProviderFetchDataException;
}
