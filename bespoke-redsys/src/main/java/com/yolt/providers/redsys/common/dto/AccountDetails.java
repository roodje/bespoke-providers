package com.yolt.providers.redsys.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AccountDetails {
    private String resourceId;

    private String iban;

    private String bban;

    private String msisdn;

    private String currency;

    private String name;

    private String product;

    private String cashAccountType;

    private String status;

    private String bic;

    private String usage;

    private String details;

    private List<Balance> balances;

    @JsonProperty("_links")
    private LinksAccountBalancesTransactions links;

    private String ownerName;
}
