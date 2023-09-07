package com.yolt.providers.unicredit.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface UniCreditBalanceDTO {

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("$.balanceAmount.amount")
    double getAmount();

    @JsonPath("$.balanceType")
    String getBalanceType();

    @JsonPath("$.lastChangeDateTime")
    String getLastChangeDateTime();

    @JsonPath("$.referenceDate")
    String getReferenceDate();

    @JsonPath("$.lastCommittedTransaction")
    String getLastCommittedTransaction();
}
