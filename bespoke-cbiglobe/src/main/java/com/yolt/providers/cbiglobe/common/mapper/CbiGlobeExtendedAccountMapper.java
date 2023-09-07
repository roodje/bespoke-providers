package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadAccountListResponseTypeAccounts;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;

public interface CbiGlobeExtendedAccountMapper {
    ExtendedAccountDTO mapExtendedAccountDtoFromCurrentAccount(ReadAccountListResponseTypeAccounts account);
}
