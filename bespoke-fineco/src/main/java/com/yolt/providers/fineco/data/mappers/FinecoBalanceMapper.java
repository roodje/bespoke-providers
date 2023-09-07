package com.yolt.providers.fineco.data.mappers;

import com.yolt.providers.fineco.v2.dto.Balance;
import com.yolt.providers.fineco.v2.dto.BalanceType;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;

import java.math.BigDecimal;
import java.util.List;

public interface FinecoBalanceMapper {
    BigDecimal createBalanceForAccount(List<Balance> balances, BalanceType balanceType);

    List<BalanceDTO> mapToBalances(List<Balance> balances);
}
