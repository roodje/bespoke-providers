package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadCardAccountListResponseTypeCardAccounts;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;

public interface CbiGlobeExtendedCardAccountMapper {
    ExtendedAccountDTO mapExtendedAccountDtoFromCurrentAccount(ReadCardAccountListResponseTypeCardAccounts account);
}
