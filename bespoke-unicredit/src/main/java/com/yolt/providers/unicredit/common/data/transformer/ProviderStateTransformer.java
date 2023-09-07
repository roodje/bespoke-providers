package com.yolt.providers.unicredit.common.data.transformer;

public interface ProviderStateTransformer<T> {
    String transformToString(T providerState);
    T transformToObject(String providerState);
}
