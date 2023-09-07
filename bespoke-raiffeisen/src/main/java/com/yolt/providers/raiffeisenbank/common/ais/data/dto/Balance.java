package com.yolt.providers.raiffeisenbank.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.LocalDate;

@ProjectedPayload
public interface Balance {

    @JsonPath("$.balanceType")
    String getType();

    @JsonPath("$.balanceAmount.amount")
    BigDecimal getAmount();

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("referenceDate")
    LocalDate getReferenceDate();
}
