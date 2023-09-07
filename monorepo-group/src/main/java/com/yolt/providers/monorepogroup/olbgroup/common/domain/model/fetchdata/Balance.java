package com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;

@ProjectedPayload
public interface Balance {

    @JsonPath("$.balanceType")
    String getBalanceType();

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("$.balanceAmount.amount")
    BigDecimal getAmount();

    @JsonPath("$.referenceDate")
    String getReferenceDate();
}
