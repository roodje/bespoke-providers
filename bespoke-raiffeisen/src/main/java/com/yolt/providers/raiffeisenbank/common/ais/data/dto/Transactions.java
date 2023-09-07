package com.yolt.providers.raiffeisenbank.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Transactions {

    @JsonPath("$.transactions.booked")
    List<Transaction> getBookedTransactions();

    @JsonPath("$.transactions.pending")
    List<Transaction> getPendingTransactions();

    @JsonPath("$.transactions._links.nextPage")
    String getNextPageUrl();

}
