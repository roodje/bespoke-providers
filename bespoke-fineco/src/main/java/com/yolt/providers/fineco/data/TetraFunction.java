package com.yolt.providers.fineco.data;

@FunctionalInterface
public interface TetraFunction<T, U, V, W, R> {
    R apply(T var1, U var2, V var3, W var4);
}
