package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

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
