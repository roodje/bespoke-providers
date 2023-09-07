package com.yolt.providers.fineco.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public class FinecoFunctions<T, U, V> {
    private final Function<T, List<U>> accountsFunction;
    private final Function<U, String> resourceIdFunction;
    private final Function<V, String> transactionsAbsoluteUrlFunction;
}
