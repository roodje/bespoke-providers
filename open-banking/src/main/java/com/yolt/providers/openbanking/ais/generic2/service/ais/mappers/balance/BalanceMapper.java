package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface BalanceMapper {
    BigDecimal getBalance(final Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType, Supplier<List<OBBalanceType1Code>> balanceTypeSupplier);

    BigDecimal getBalance(String amount, boolean shouldNegate);
}
