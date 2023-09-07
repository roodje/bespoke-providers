package com.yolt.providers.belfius.common.model.ais;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;

@ProjectedPayload
public interface Account {

    @JsonPath("$.type")
    String getType();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.balance")
    BigDecimal getBalance();

    @JsonPath("$.accountName")
    String getAccountName();
}
