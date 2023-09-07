package com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Account {

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.cashAccountType")
    String getCashAccountType();

    @JsonPath("$.name")
    String getName();

    @JsonPath("$.status")
    String getStatus();

    @JsonPath("$.balances")
    List<Balance> getBalances();
}
