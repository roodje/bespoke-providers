package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ProjectedPayload
public interface Balance {

    @JsonPath("$.balanceAmount.amount")
    BigDecimal getAmount();

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("$.lastChangeDateTime")
    LocalDateTime getLastChangeDateTime();

    @JsonPath("$.referenceDate")
    LocalDate getReferenceDate();

    @JsonPath("$.balanceType")
    String getBalanceType();

}
