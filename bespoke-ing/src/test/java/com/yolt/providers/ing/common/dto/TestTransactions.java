package com.yolt.providers.ing.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestTransactions implements Transactions {
    public List<Transaction> bookedTransactions;
    public List<Transaction> pendingTransactions;
    public String nextPageUrl;
}
