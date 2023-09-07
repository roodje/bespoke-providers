package com.yolt.providers.openbanking.ais.generic2.claims.producer;

public class FapiCompliantJwtClaimsProducerDecorator extends ExpiringJwtClaimsProducerDecorator {
    private static final int JWT_EXPIRATION_TIME_IN_MINUTES = 60;

    public FapiCompliantJwtClaimsProducerDecorator(JwtClaimsProducer wrapee) {
        super(wrapee, JWT_EXPIRATION_TIME_IN_MINUTES);
    }
}
