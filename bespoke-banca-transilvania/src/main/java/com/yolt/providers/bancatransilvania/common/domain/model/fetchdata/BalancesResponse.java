package com.yolt.providers.bancatransilvania.common.domain.model.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface BalancesResponse {

    @JsonPath("$.account")
    Account getAccount();

    @JsonPath("$.balances")
    List<Balance> getBalances();
}
