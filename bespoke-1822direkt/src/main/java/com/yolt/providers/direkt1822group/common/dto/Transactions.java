package com.yolt.providers.direkt1822group.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transactions {

    private List<Transaction> booked;
    private List<Transaction> pending;

    @JsonProperty("_links")
    private Links links;

}
