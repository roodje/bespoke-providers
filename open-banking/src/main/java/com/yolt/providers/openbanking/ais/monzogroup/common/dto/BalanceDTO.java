package com.yolt.providers.openbanking.ais.monzogroup.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BalanceDTO {

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("Currency")
    private String currency;
}