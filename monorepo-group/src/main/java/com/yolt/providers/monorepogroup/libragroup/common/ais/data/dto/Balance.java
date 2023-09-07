package com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.LocalDate;

@ProjectedPayload
public interface Balance {

    @JsonPath("$.balanceAmount.amount")
    BigDecimal getAmount();

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("$.lastChangeDateTime")
    String getLastChangeDateTime();

    @JsonPath("$.referenceDate")
    LocalDate getReferenceDate();

    @JsonPath("$.balanceType")
    String getBalanceType();

}
