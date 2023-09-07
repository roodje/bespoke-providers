package com.yolt.providers.openbanking.ais.monzogroup.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReadPot {

    @JsonProperty("Data")
    private ReadPotData data;
}