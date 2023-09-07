package com.yolt.providers.triodosbank.common.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Access {
    private List<Account> accounts;
    private List<Balance> balances;
    private List<Transaction> transactions;

    public Access() {
        this.accounts = new ArrayList<>();
        this.balances = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }
}
