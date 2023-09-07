package com.yolt.providers.openbanking.ais.generic2.claims.producer;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import lombok.AllArgsConstructor;
import org.jose4j.jwt.JwtClaims;

@AllArgsConstructor
public class ExpiringJwtClaimsProducerDecorator implements JwtClaimsProducer {

    private final JwtClaimsProducer wrapee;
    private final int expirationTimeInMinutes;

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = wrapee.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope, args);
        claims.setExpirationTimeMinutesInTheFuture(expirationTimeInMinutes);
        return claims;
    }
}
