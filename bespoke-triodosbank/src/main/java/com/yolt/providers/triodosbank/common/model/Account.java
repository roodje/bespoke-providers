package com.yolt.providers.triodosbank.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    private String iban;
    private String currency;
    private String resourceId;
    private String cashAccountType;
    private String name;
    private String status;

    @JsonProperty("_links")
    private AccountLinks links;

    public String getBalancesUrl() {
        return links.getBalances();
    }

    public String getTransactionsUrl() {
        return links.getTransactions();
    }
}
