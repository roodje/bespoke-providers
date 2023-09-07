package com.yolt.providers.abnamrogroup.common.pis;

import java.util.UUID;

public class AbnAmroRandomUuidXRequestIdHeaderProvider implements AbnAmroXRequestIdHeaderProvider {

    @Override
    public String provideXRequestIdHeader() {
        return UUID.randomUUID().toString();
    }
}
