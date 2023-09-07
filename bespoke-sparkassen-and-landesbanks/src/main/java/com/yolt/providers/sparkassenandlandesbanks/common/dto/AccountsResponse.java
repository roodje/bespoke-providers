package com.yolt.providers.sparkassenandlandesbanks.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.util.List;

@ProjectedPayload
public interface AccountsResponse {

    @JsonPath("$.accounts")
    List<Account> getAccounts();

    interface Account {

        @JsonPath("$.resourceId")
        String getResourceId();

        @JsonPath("$.iban")
        String getIban();

        @JsonPath("$.currency")
        String getCurrency();

        @JsonPath("$.product")
        String getProduct();

        @JsonPath("$.cashAccountType")
        String getCashAccountType();

        @JsonPath("$.name")
        String getName();

        @JsonPath("$.balances")
        List<Balance> getBalances();

        @JsonPath("$.ownerName")
        String getOwnerName();

        interface Balance {

            @JsonPath("$.balanceType")
            String getBalanceType();

            @JsonPath("$.balanceAmount.amount")
            BigDecimal getBalanceAmount();

            @JsonPath("$.balanceAmount.currency")
            String getBalanceCurrency();
        }
    }
}
