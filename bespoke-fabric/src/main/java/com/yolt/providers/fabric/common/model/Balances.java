package com.yolt.providers.fabric.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Balances {
    @JsonPath("$.balances")
    List<Balance> getBalances();
}
