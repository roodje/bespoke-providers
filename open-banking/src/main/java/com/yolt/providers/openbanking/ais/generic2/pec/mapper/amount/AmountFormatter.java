package com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount;

import java.math.BigDecimal;

public interface AmountFormatter {

    String format(BigDecimal amount);
}
