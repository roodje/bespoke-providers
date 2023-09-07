package com.yolt.providers.openbanking.ais.generic2.pec.common;

import java.util.UUID;

public class RandomUUIDPaymentRequestKeyProvider implements PaymentRequestIdempotentKeyProvider {

    @Override
    public String provideIdempotentKey(Object parameter) {
        return UUID.randomUUID().toString();
    }
}
