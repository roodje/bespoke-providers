package com.yolt.providers.axabanque.common.model.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Balance {

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("$.balanceAmount.amount")
    double getAmount();

    @JsonPath("$.balanceType")
    String getType();

    @JsonPath("$.lastChangeDateTime")
    String getLastChangeDate();

    @JsonPath("$.referenceDate")
    String getReferenceDate();

    @JsonPath("$.lastCommittedTransaction")
    String getLastCommittedTransaction();
}