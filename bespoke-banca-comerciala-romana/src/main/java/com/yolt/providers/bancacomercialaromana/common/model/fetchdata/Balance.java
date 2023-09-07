package com.yolt.providers.bancacomercialaromana.common.model.fetchdata;

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
    String getAmount();

    @JsonPath("$.referenceDate")
    String getReferenceDate();

    @JsonPath("$.creditLimitIncluded")
    String getCreditLimitIncluded();

    default BigDecimal getDecimalAmount() {
        return new BigDecimal(getAmount());
    }
}
