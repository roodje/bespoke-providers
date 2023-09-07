package com.yolt.providers.fabric.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Balance {

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("$.balanceAmount")
    Amount getBalanceAmount();

    @JsonPath("$.balanceType")
    String getType();

    @JsonPath("$.lastChangeDateTime")
    String getLastChangeDate();

    @JsonPath("$.lastCommittedTransaction")
    String getLastCommittedTransaction();
}