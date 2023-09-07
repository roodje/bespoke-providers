package com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Account {

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.accountName")
    String getAccountName();

    @JsonPath("$.workingBalance")
    String getWorkingBalance();

    @JsonPath("$.onlineActualBalance")
    String getOnlineActualBalance();

    @JsonPath("$.customerName")
    String getCustomerName();

    @JsonPath("$.category")
    String getCategory();

}
