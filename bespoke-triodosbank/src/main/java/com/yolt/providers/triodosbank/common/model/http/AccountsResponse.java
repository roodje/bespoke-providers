package com.yolt.providers.triodosbank.common.model.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yolt.providers.triodosbank.common.model.Account;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountsResponse {

    private List<Account> accounts;
}
