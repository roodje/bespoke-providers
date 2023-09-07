package com.yolt.providers.abancagroup.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@ProjectedPayload
public interface Transaction {

    @JsonPath("$.id")
    String getTransactionId();

    @JsonPath("$.attributes.valueDate")
    LocalDateTime getValueDate();

    @JsonPath("$.attributes.operationDate")
    LocalDateTime getOperationDate();

    @JsonPath("$.attributes.concept")
    String getConcept();

    @JsonPath("$.attributes.amount.value")
    BigDecimal getAmount();

    @JsonPath("$.attributes.amount.currency")
    String getCurrency();

}
