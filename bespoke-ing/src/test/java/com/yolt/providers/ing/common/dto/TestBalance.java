package com.yolt.providers.ing.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TestBalance implements Balances.Balance {
    public String type;
    public BigDecimal amount;
    public String currency;
    public String lastChangeDate;
    public String referenceDate;
}
