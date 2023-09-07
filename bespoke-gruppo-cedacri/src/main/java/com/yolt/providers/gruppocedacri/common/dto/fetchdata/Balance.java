package com.yolt.providers.gruppocedacri.common.dto.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;

@ProjectedPayload
public interface Balance {

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("$.balanceAmount.amount")
    String getAmount();

    @JsonPath("$.balanceType")
    String getBalanceType();

    @JsonPath("$.referenceDate")
    String getReferenceDate();

    default BigDecimal getDecimalAmount() {
        return new BigDecimal(getAmount());
    }
}
