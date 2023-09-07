package com.yolt.providers.openbanking.ais.generic2.claims.producer;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;

import java.util.function.Function;

import static com.yolt.providers.common.constants.OAuth.MAX_AGE;

public class ExtendedJwtClaimProducer extends DefaultJwtClaimsProducer {

    public ExtendedJwtClaimProducer(Function<DefaultAuthMeans, String> issuerSupplier, String audience) {
        super(issuerSupplier, audience);
    }

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans,
                                     String secretState,
                                     String redirectUrl,
                                     TokenScope scope,
                                     String... args) {
        JwtClaims claims = super.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope, args);
        claims.setSubject(authenticationMeans.getClientId());
        claims.setExpirationTimeMinutesInTheFuture(10);
        claims.setIssuedAtToNow();
        claims.setGeneratedJwtId();
        claims.unsetClaim(MAX_AGE);
        return claims;
    }
}
