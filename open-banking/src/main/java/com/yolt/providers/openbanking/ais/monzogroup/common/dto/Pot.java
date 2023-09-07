package com.yolt.providers.openbanking.ais.monzogroup.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Pot {

    @JsonProperty("PotId")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("CreditDebitIndicator")
    private String creditDebitIndicator;

    @JsonProperty("Style")
    private String style;

    @JsonProperty("Created")
    private String created;

    @JsonProperty("Updated")
    private String updated;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Balance")
    private BalanceDTO balance;
}