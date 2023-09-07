package com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Account {

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.name")
    String getName();

    @JsonPath("$.bic")
    String getBic();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.cashAccountType")
    String getCashAccountType();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.product")
    String getProduct();

    @JsonPath("$.balances")
    List<Balance> getBalances();
}
