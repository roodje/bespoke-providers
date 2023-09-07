package com.yolt.providers.openbanking.ais.generic2.pec.common;

public interface PaymentRequestIdempotentKeyProvider {

    String provideIdempotentKey(Object parameter);

    default String provideIdempotentKey() {
        return provideIdempotentKey(null);
    }
}
