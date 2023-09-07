package com.yolt.providers.openbanking.ais.danske.service.claim;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExpiringJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;

public class DanskeJwtClaimProducer extends ExpiringJwtClaimsProducerDecorator {

    public DanskeJwtClaimProducer(JwtClaimsProducer wrapee, int expirationTimeInMinutes) {
        super(wrapee, expirationTimeInMinutes);
    }

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = super.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope, args);
        claims.setSubject(authenticationMeans.getClientId());
        claims.setIssuedAtToNow();
        claims.setGeneratedJwtId();
        return claims;
    }
}
