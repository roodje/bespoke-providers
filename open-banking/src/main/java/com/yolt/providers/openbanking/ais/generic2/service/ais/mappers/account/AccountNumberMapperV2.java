package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;

import java.util.List;

public interface AccountNumberMapperV2 {

    ProviderAccountNumberDTO map(OBAccount4Account account, List<PartyDto> parties);
}
