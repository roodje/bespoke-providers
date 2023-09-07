package com.yolt.providers.monorepogroup.chebancagroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface TransactionResponse {

    @JsonPath("$.data.transactionsAccounting")
    List<Transaction> getBookedTransactions();

    @JsonPath("$.data.transactionsNotAccounting")
    List<Transaction> getPendingTransactions();

    @JsonPath("$._links.next.href")
    String getNextHref();
}
