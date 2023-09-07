package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.LocalDate;

@ProjectedPayload
public interface Balance {

    @JsonPath("$.balanceAmount.currency")
    String getCurrency();

    @JsonPath("$.balanceAmount.amount")
    BigDecimal getAmount();

    @JsonPath("$.balanceType")
    RaiffeisenBalanceType getBalanceType();

    @JsonPath("$.referenceDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    LocalDate getReferenceDate();
}
