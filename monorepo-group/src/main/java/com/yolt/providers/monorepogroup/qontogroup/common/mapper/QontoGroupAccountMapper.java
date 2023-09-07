package com.yolt.providers.monorepogroup.qontogroup.common.mapper;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

public interface QontoGroupAccountMapper {

    ProviderAccountDTO.ProviderAccountDTOBuilder map(Account account);
}
