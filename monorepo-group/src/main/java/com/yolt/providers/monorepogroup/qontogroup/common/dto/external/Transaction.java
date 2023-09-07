package com.yolt.providers.monorepogroup.qontogroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@ProjectedPayload
public interface Transaction {

    @JsonPath("$.transaction_id")
    String getTransactionId();

    @JsonPath("$.amount")
    BigDecimal getAmount();

    @JsonPath("$.side")
    String getSide();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.label")
    String getLabel();

    @JsonPath("emitted_at")
    OffsetDateTime getEmittedAt();

    @JsonPath("settled_at")
    OffsetDateTime getSettledAt();

    @JsonPath("$.status")
    String getStatus();

    @JsonPath("reference")
    String getReference();
}
