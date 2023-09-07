package com.yolt.providers.openbanking.ais.nationwide.service.claims;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExpiringJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;

public class NationwideExpiringJwtClaimsProducer extends ExpiringJwtClaimsProducerDecorator {

    public NationwideExpiringJwtClaimsProducer(JwtClaimsProducer wrappee, int expirationTimeInMinutes) {
        super(wrappee, expirationTimeInMinutes);
    }

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = super.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope);
        claims.setIssuedAtToNow();
        return claims;
    }
}
