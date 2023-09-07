package com.yolt.providers.gruppocedacri.common.dto.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface BalancesResponse {

    @JsonPath("$.balances")
    List<Balance> getBalances();
}
