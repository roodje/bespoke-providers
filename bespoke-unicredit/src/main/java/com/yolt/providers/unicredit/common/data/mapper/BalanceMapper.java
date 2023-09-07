package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.unicredit.common.dto.UniCreditBalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;

import java.math.BigDecimal;
import java.util.List;

public interface BalanceMapper {
    BigDecimal getBalanceAmount(final List<UniCreditBalanceDTO> balances, final BalanceType balanceType);
    BalanceDTO mapBalance(final UniCreditBalanceDTO balance);
}
