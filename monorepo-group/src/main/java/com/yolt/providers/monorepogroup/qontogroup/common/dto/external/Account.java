package com.yolt.providers.monorepogroup.qontogroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@ProjectedPayload
public interface Account {

    @JsonPath("$.slug")
    String getSlug();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.bic")
    String getBic();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.balance")
    BigDecimal getBalance();

    @JsonPath("$.authorized_balance")
    BigDecimal getAuthorizedBalance();

    @JsonPath("$.name")
    String getName();

    @JsonPath("$.updated_at")
    OffsetDateTime getUpdatedAt();

    @JsonPath("$.status")
    String getStatus();
}
