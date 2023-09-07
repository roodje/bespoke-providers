package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external;

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
    String getName();

    @JsonPath("$.product")
    String getProduct();

    @JsonPath("$.cashAccountType")
    String getCashAccountType();

    @JsonPath("$.status")
    String getStatus();

    @JsonPath("$.bic")
    String getBic();
    
    @JsonPath("$.details")
    String getDetails();

    @JsonPath("$.balances")
    List<Balance> getBalances();
}
