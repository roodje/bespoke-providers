package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.util.List;
import java.util.Map;

public interface AccountMapperV2 {
    ProviderAccountDTO mapToProviderAccount(final OBAccount6 account,
                                            final List<ProviderTransactionDTO> transactions,
                                            final Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType,
                                            final List<StandingOrderDTO> standingOrders,
                                            final List<DirectDebitDTO> directDebits,
                                            final List<PartyDto> parties);
}
