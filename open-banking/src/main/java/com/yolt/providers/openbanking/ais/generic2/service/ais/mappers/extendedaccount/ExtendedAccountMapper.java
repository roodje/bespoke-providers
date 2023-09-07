package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;

import java.util.List;

public interface ExtendedAccountMapper {
    ExtendedAccountDTO mapToExtendedModelAccount(final OBAccount6 account,
                                                 final String extractedPrimaryAccountName,
                                                 final List<OBReadBalance1DataBalance> balances);
}
