package com.yolt.providers.monorepogroup.qontogroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Transactions {

    @JsonPath("$.transactions")
    List<Transaction> getTransactions();

    @JsonPath("$.meta.next_page")
    String getNextPage();
}
