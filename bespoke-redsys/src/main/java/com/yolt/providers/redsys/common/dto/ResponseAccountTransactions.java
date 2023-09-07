package com.yolt.providers.redsys.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseAccountTransactions {
    private AccountReference account;

    private List<Balance> balances;

    private Transactions transactions;
}
