package com.yolt.providers.stet.generic.http.error;

import com.yolt.providers.common.exception.TokenInvalidException;

@FunctionalInterface
public interface ExecutionSupplier<T> {

    T get() throws TokenInvalidException;
}
