package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface MonetaryAccountResponse extends PaginatedResponse {
    @JsonPath("$.Response")
    List<MonetaryAccount> getMonetaryAccounts();

    interface MonetaryAccount {
        @JsonPath("$..id")
        String getId();

        @JsonPath("$..currency")
        String getCurrency();

        @JsonPath("$..description")
        String getDescription();

        @JsonPath("$..status")
        String getStatus();

        @JsonPath("$..balance")
        Amount getBalance();

        @JsonPath("$..alias[?(@.type == \"IBAN\")].value")
        String getIban();

        @JsonPath("$..alias[?(@.type == \"IBAN\")].name")
        String getHolderName();
    }
}
