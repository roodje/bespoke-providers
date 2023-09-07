package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadCardAccountBalancesResponseTypeBalances;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;

public interface CbiGlobeCardBalanceMapper {

    BigDecimal extractBalanceAmount(List<BalanceDTO> balances,
                                    CurrencyCode supportedCurrency,
                                    List<BalanceType> preferredBalanceTypes);

    BalanceDTO mapToBalanceDTO(ReadCardAccountBalancesResponseTypeBalances balance);
}
