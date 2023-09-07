package com.yolt.providers.fineco.dto;

import lombok.Data;

import java.util.List;

@Data
public class FinecoAccount<T, U, V> {
    private T account;
    private List<U> transactions;
    private V balances;
}