package com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Transactions {

    @JsonPath("$.transactions.booked")
    List<Transaction> getTransactions();

}
