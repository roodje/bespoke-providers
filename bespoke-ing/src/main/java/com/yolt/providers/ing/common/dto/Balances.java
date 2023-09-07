package com.yolt.providers.ing.common.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.util.List;

@ProjectedPayload
public interface Balances {

    @JsonPath("$.balances")
    List<Balance> getData();

    interface Balance {

        @JsonPath("$.balanceType")
        String getType();

        @JsonPath("$.balanceAmount.amount")
        BigDecimal getAmount();

        @JsonPath("$.balanceAmount.currency")
        String getCurrency();

        @JsonPath("$.lastChangeDateTime")
        String getLastChangeDate();

        @JsonPath("$.referenceDate")
        String getReferenceDate();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    enum Type {
        EXPECTED("expected"),
        INTERIMBOOKED("interimBooked"),
        INTERIMAVAILABLE("interimAvailable");

        private final String value;
    }
}
