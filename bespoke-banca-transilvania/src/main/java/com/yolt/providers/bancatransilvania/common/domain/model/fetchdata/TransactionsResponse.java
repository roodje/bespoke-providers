package com.yolt.providers.bancatransilvania.common.domain.model.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface TransactionsResponse {

    @JsonPath("$.account")
    Account getAccount();

    @JsonPath("$.transactions.booked")
    List<Transaction> getBookedTransactions();

    @JsonPath("$.transactions.pending")
    List<Transaction> getPendingTransactions();

    @JsonPath("$._links.next.href")
    String getNextHref();
}
