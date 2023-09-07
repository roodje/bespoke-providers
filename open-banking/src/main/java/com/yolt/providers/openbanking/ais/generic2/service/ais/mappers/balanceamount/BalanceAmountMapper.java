package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;

public interface BalanceAmountMapper {
    BalanceAmountDTO apply(OBTransaction6 transaction);

    BalanceAmountDTO apply(OBReadBalance1DataBalance balance);
}
