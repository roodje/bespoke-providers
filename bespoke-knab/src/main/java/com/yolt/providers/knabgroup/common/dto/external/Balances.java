package com.yolt.providers.knabgroup.common.dto.external;

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

        @JsonPath("$.lastCommittedTransaction")
        String getLastCommittedTransaction();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    enum Type {
        INTERIMBOOKED("interimBooked"),
        INTERIMAVAILABLE("interimAvailable");

        private final String value;
    }
}
