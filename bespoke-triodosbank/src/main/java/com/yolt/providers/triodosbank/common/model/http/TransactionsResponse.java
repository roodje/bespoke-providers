package com.yolt.providers.triodosbank.common.model.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yolt.providers.triodosbank.common.model.Account;
import com.yolt.providers.triodosbank.common.model.Transactions;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionsResponse {

    private Account account;
    private Transactions transactions;
}
