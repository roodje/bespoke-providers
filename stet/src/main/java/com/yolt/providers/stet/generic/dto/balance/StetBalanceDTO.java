package com.yolt.providers.stet.generic.dto.balance;

import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@ProjectedPayload
public interface StetBalanceDTO {

    @JsonPath("$.name")
    String getName();

    @JsonPath("$.balanceAmount.amount")
    BigDecimal getAmount();

    @JsonPath("$.balanceAmount.currency")
    CurrencyCode getCurrency();

    @JsonPath("$.balanceType")
    StetBalanceType getType();

    @JsonPath("$.lastChangeDateTime")
    OffsetDateTime getLastChangeDateTime();

    @JsonPath("$.referenceDate")
    OffsetDateTime getReferenceDate();

    @JsonPath("$.lastCommittedTransaction")
    String getLastCommittedTransaction();
}
