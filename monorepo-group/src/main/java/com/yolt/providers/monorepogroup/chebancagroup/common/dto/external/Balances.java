package com.yolt.providers.monorepogroup.chebancagroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.LocalDate;

@ProjectedPayload
public interface Balances {

    @JsonPath("$.data.accountBalance.currency")
    String getAccountCurrency();

    @JsonPath("$.data.accountBalance.amount")
    BigDecimal getAccountAmount();

    @JsonPath("$.data.availableBalance.currency")
    String getAvailableBalanceCurrency();

    @JsonPath("$.data.availableBalance.amount")
    BigDecimal getAvailableBalanceAmount();

    @JsonPath("$.data.date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    LocalDate getReferenceDate();

}
