package com.yolt.providers.abancagroup.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;

@ProjectedPayload
public interface Balance {

    @JsonPath("$.data.attributes.availableBalance.value")
    BigDecimal getAmount();

    @JsonPath("$.data.attributes.availableBalance.currency")
    String getCurrency();

}
