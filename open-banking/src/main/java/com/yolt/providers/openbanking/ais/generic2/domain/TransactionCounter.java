package com.yolt.providers.openbanking.ais.generic2.domain;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TransactionCounter {
    private int counter;
    private final Set<Integer> dataHashCodes = new HashSet<>();
    private String pages;

    public TransactionCounter(String pages, int dataHashCode) {
        this.counter = 1;
        this.pages = pages;
        this.dataHashCodes.add(dataHashCode);
    }
}
