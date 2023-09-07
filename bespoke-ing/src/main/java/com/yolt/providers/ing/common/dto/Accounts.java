package com.yolt.providers.ing.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Accounts {

    @JsonPath("$.accounts")
    List<Account> getData();

    interface Account {

        @JsonPath("$.resourceId")
        String getId();

        @JsonPath("$.iban")
        String getIban();

        @JsonPath("$.maskedPan")
        String getMaskedPan();

        @JsonPath("$.name")
        String getName();

        @JsonPath("$.currency")
        String getCurrency();

        @JsonPath("$.product")
        String getProduct();

        @JsonPath("$._links.transactions.href")
        String getTransactionLink();

        @JsonPath("$._links.balances.href")
        String getBalancesLink();
    }
}
