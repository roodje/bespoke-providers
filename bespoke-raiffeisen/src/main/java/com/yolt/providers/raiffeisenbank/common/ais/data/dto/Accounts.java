package com.yolt.providers.raiffeisenbank.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Accounts {

    @JsonPath("$.accounts")
    List<Account> getAccounts();
}
