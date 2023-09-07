package com.yolt.providers.knabgroup.common.dto.external;

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

        @JsonPath("$.bban")
        String getBban();

        @JsonPath("$.product")
        String getProduct();

        @JsonPath("$.name")
        String getName();

        @JsonPath("$.currency")
        String getCurrency();

        @JsonPath("$.cashAccountType")
        String getAccountType();
    }
}
