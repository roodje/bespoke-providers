package com.yolt.providers.openbanking.ais.monzogroup.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReadPotData {

    @JsonProperty("Pot")
    private List<Pot> pots;
}