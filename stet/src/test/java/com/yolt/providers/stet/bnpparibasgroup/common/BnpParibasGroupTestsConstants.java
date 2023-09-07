package com.yolt.providers.stet.bnpparibasgroup.common;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.util.UUID;

public class BnpParibasGroupTestsConstants {

    private BnpParibasGroupTestsConstants() {
        throw new IllegalStateException();
    }

    public static final UUID USER_ID = UUID.fromString("07a540a2-7b91-11e9-8f9e-2a86e4085a59");
    public static final String STATE = UUID.fromString("e35eaf8f-5e22-411d-88cc-7301a5c72728").toString();
    public static final String ACCESS_TOKEN = "access-token";
    public static final String ACCESS_TOKEN_INVALID = "access-token-invalid";
    public static final String ACCESS_TOKEN_EMPTY = "access-token-empty";
    public static final Instant TRANSACTIONS_FETCH_START_TIME = Instant.now(Clock.systemUTC().withZone(ZoneId.of("Europe/Paris")))
            .minus(Period.ofDays(534));
    public static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
}
