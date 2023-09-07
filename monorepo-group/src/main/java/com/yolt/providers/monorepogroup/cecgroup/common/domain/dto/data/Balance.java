package com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data;

import org.springframework.data.web.JsonPath;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface Balance {

    @JsonPath("$.balanceAmount.amount")
    BigDecimal getAmount();

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("$.referenceDate")
    LocalDate getReferenceDate();

    @JsonPath("$.balanceType")
    String getBalanceType();
}
