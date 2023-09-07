package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;

import java.math.BigDecimal;

public interface Amount {
    @JsonPath("$.value")
    BigDecimal getValue();

    @JsonPath("$.currency")
    String getCurrency();
}
