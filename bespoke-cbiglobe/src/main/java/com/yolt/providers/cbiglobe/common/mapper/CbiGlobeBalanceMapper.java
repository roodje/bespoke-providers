package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadAccountBalanceResponseTypeBalances;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;

public interface CbiGlobeBalanceMapper {
    BigDecimal extractBalanceAmount(List<BalanceDTO> balances,
                                    CurrencyCode supportedCurrency,
                                    List<BalanceType> preferredBalanceTypes);

    BalanceDTO mapToBalanceDTO(ReadAccountBalanceResponseTypeBalances balance);
}
