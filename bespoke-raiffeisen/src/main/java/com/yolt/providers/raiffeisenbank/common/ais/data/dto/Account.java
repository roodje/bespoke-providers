package com.yolt.providers.raiffeisenbank.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Account {

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.name")
    String getAccountName();

    @JsonPath("$.product")
    String getProduct();

    @JsonPath("$.cashAccountType")
    String getCashAccountType();

    @JsonPath("$.bic")
    String getBic();

    @JsonPath("$.ownerName")
    String getOwnerName();

    @JsonPath("$.balances")
    List<Balance> getBalances();
}
