package com.yolt.providers.redsys.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Transactions {
    private List<Transaction> booked;

    private List<Transaction> pending;

    @JsonProperty("_links")
    private LinksAccountsTransactions links;
}