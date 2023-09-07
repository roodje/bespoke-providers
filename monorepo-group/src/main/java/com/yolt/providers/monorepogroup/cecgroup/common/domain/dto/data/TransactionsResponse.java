package com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface TransactionsResponse {

    @JsonPath("$.transactions.booked")
    List<Transaction> getBookedTransactions();

    @JsonPath("$.transactions.pending")
    List<Transaction> getPendingTransactions();

    @JsonPath("$.transactions._links.next.href")
    String getNextPageUrl();
}
