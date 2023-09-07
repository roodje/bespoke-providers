package com.yolt.providers.abancagroup.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Accounts {

    @JsonPath("$.data")
    List<Account> getAccounts();
}
