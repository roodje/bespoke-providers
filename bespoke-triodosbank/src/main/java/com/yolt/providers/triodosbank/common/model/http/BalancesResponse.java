package com.yolt.providers.triodosbank.common.model.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yolt.providers.triodosbank.common.model.Account;
import com.yolt.providers.triodosbank.common.model.Balance;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalancesResponse {

    private Account account;
    private List<Balance> balances;
}
