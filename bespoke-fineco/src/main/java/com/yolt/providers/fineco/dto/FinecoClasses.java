package com.yolt.providers.fineco.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FinecoClasses<T, U, V, W> {
    private final Class<T> accounts;
    private final Class<U> account;
    private final Class<V> transactions;
    private final Class<W> balances;
}
