package com.yolt.providers.triodosbank.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transactions {

    private List<Transaction> booked = new ArrayList<>();
    private List<Transaction> pending = new ArrayList<>();

    @JsonProperty("_links")
    private TransactionLinks links;
}
