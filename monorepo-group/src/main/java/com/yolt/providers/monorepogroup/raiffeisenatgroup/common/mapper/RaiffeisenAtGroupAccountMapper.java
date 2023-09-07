package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Account;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

public interface RaiffeisenAtGroupAccountMapper {

    ProviderAccountDTO.ProviderAccountDTOBuilder map(Account account);
}
